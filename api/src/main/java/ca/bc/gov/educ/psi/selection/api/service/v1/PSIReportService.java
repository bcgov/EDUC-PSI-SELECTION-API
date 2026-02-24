package ca.bc.gov.educ.psi.selection.api.service.v1;

import ca.bc.gov.educ.psi.selection.api.exception.PSISelectionAPIRuntimeException;
import ca.bc.gov.educ.psi.selection.api.model.v1.sts.OrderEntity;
import ca.bc.gov.educ.psi.selection.api.model.v1.sts.OrderItemEntity;
import ca.bc.gov.educ.psi.selection.api.repository.v1.OrderRepository;
import ca.bc.gov.educ.psi.selection.api.rest.RestUtils;
import ca.bc.gov.educ.psi.selection.api.struct.v1.DownloadableReportResponse;
import ca.bc.gov.educ.psi.selection.api.struct.v1.PSIOrderRow;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.gradProgram.GraduationProgramCode;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.institute.SchoolTombstone;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.student.Student;
import com.nimbusds.jose.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.psi.selection.api.constants.v1.reports.PSIReportHeaders.*;
import static ca.bc.gov.educ.psi.selection.api.constants.v1.reports.ReportTypeCodes.PSI_REPORT;

@Service
@Slf4j
public class PSIReportService {
    private final OrderRepository orderRepository;
    private final RestUtils restUtils;

    public PSIReportService(OrderRepository orderRepository, RestUtils restUtils) {
        this.orderRepository = orderRepository;
        this.restUtils = restUtils;
    }

    public DownloadableReportResponse generateReport(SchoolTombstone school) {
        var programs = restUtils.getGraduationProgramCodeList(true).stream().map(GraduationProgramCode::getProgramCode).toList();
        // Send a schoolID to grad student -> receive UUID of students based on criteria (currently just schoolID)
        List<UUID> studentGradRecordIDs = restUtils.getGradStudentUUIDsFromSchoolID(List.of(UUID.fromString(school.getSchoolId())), programs, List.of("12", "AD"));
        log.debug("studentGradRecordIDs: {}", studentGradRecordIDs);

        // Get student details from student API for each studentGradRecordID UUID
        List<Student> students = restUtils.getStudentDetailsByUUIDs(studentGradRecordIDs);
        log.debug("Fetched {} student details", students.size());
        log.debug("first fetched student if available: {}", students.stream().findFirst().orElse(null));

        // oct 1st -> Sept 30 
        var currentReportingPeriod = getCurrentReportingPeriod();

        Set<String> studentPens = students.stream().map(Student::getPen).collect(Collectors.toSet());
        log.debug("Fetching orders for {} students", students.size());
        Map<String, List<PSIOrderRow>> orderMap = orderRepository
                .findOrderRowsByStudentPensAndDateRange(studentPens, currentReportingPeriod.getLeft(), currentReportingPeriod.getRight())
                .stream()
                .collect(Collectors.groupingBy(PSIOrderRow::studentPen));
        log.debug("Order fetch complete");

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(SURNAME.getCode(), FIRST_NAME.getCode(), MIDDLE_NAMES.getCode(), LOCAL_ID.getCode(), PEN.getCode(), ORDER_PLACED.getCode(), PSI_NAME.getCode(), TRANSMISSION_MODE.getCode(), ORDER_TYPE.getCode())
                .build();

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
            
            for (Student student : students) {
                if (orderMap.containsKey(student.getPen())) {
                    for (PSIOrderRow row : orderMap.get(student.getPen())) {
                        String psiName = restUtils.getPsiByCode(row.psiCode())
                                .map(psi -> psi.getPsiName())
                                .orElse("PSI name not found");
    
                        String transmissionMode = row.transmissionMode();
                        String orderType = resolveOrderType(row);
    
                        csvPrinter.printRecord(prepareDataForCsv(student, psiName, transmissionMode, orderType));
                    }
                } else {
                    csvPrinter.printRecord(prepareDataForCsv(student, "Not Applicable", "Not Applicable", "Not Applicable"));
                }
            }

            csvPrinter.flush();

            var downloadableReport = new DownloadableReportResponse();
            downloadableReport.setReportType(PSI_REPORT.getCode());
            downloadableReport.setReportName(String.format("%s - PSI Selection Report - %s", school.getMincode(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
            downloadableReport.setDocumentData(Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            return downloadableReport;

        } catch (IOException e) {
            throw new PSISelectionAPIRuntimeException(e.toString());
        }
    }

    private String resolveOrderType(PSIOrderRow row) {
        if (StringUtils.isBlank(row.transmissionMode())) {
            return "Not Applicable";
        }
        if (row.transmissionMode().equalsIgnoreCase("PAPER")) {
            return StringUtils.isNotBlank(row.ecmPsiMailBtcID()) ? "Send Now" : "End of July";
        }
        if (row.transmissionMode().equalsIgnoreCase("XML")) {
            return row.authUntilDate() != null
                    ? "Ongoing updates until " + row.authUntilDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    : "One-Time";
        }
        return "Not Applicable";
    }
    
    private Pair<LocalDateTime, LocalDateTime> getCurrentReportingPeriod(){
        var now = LocalDateTime.now();
        if(now.getMonthValue() < 10){
            return Pair.of(LocalDateTime.now().withYear(now.getYear()-1).withMonth(10).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0),
                    LocalDateTime.now().withYear(now.getYear()).withMonth(9).withDayOfMonth(30).withHour(23).withMinute(59).withSecond(59));
        }
        return Pair.of(LocalDateTime.now().withYear(now.getYear()).withMonth(10).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0),
                LocalDateTime.now().withYear(now.getYear()+1).withMonth(9).withDayOfMonth(30).withHour(23).withMinute(59).withSecond(59));
    }

    public List<String> prepareDataForCsv(Student student, String psiName, String transmissionMode, String orderType) {
        var orderPlaced = StringUtils.isBlank(orderType) || orderType.equalsIgnoreCase("Not Applicable") ? "N" : "Y";
        return Arrays.asList(
                student.getLegalLastName(),
                student.getLegalFirstName(),
                student.getLegalMiddleNames(),
                student.getLocalID(),
                student.getPen(),
                orderPlaced,
                psiName,
                transmissionMode,
                orderType
        );
    }
}
