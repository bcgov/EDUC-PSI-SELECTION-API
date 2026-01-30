package ca.bc.gov.educ.psi.selection.api.service.v1;

import ca.bc.gov.educ.psi.selection.api.exception.PSISelectionAPIRuntimeException;
import ca.bc.gov.educ.psi.selection.api.model.v1.sts.OrderEntity;
import ca.bc.gov.educ.psi.selection.api.model.v1.sts.OrderItemEntity;
import ca.bc.gov.educ.psi.selection.api.repository.v1.OrderRepository;
import ca.bc.gov.educ.psi.selection.api.rest.RestUtils;
import ca.bc.gov.educ.psi.selection.api.struct.v1.DownloadableReportResponse;
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
        List<OrderEntity> orderEntities = orderRepository.findAllByCreateDateBetweenAndOrderDateIsNotNull(currentReportingPeriod.getLeft(), currentReportingPeriod.getRight());
        log.debug("Fetched {} order entity records", orderEntities.size());
        log.debug("First fetched order record if available: {}", orderEntities.stream().findFirst().orElse(null));

        Map<String, List<OrderEntity>> orderMap = orderEntities.stream()
                .filter(order -> !order.getStudentXrefEntities().isEmpty() && order.getStudentXrefEntities().stream().findFirst().get().getStudentPENEntity() != null)
                .collect(Collectors.groupingBy(order -> order.getStudentXrefEntities().stream().findFirst().get().getStudentPENEntity().getStudentPen()));

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(SURNAME.getCode(), FIRST_NAME.getCode(), MIDDLE_NAMES.getCode(), LOCAL_ID.getCode(), PEN.getCode(), ORDER_PLACED.getCode(), PSI_NAME.getCode(), TRANSMISSION_MODE.getCode(), ORDER_TYPE.getCode())
                .build();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

            for (Student student : students) {
                String psiName;
                String transmissionMode;
                String orderType;
                if(orderMap.containsKey(student.getPen())) {
                    for(OrderEntity order: orderMap.get(student.getPen())){
                        for(OrderItemEntity orderItem: order.getOrderItemEntities()){
                            psiName = "PSI name not found";
                            transmissionMode = "Not Applicable";
                            orderType = "Not Applicable";
                            var delivery = orderItem.getDeliveryInfoEntity();
                            var infoType = delivery.getInfoType();
                            if(StringUtils.isNotBlank(infoType) && infoType.equalsIgnoreCase("PSI_PREF")) {
                                transmissionMode = delivery.getTransmissionMode();
                                var psi = restUtils.getPsiByCode(delivery.getPsiCode());
                                if (psi.isPresent()) {
                                    psiName = psi.get().getPsiName();
                                }
          
                                if(StringUtils.isNotBlank(transmissionMode)) {
                                    if (transmissionMode.equalsIgnoreCase("PAPER")) {
                                        if (StringUtils.isNotBlank(orderItem.getEcmPsiMailBtcID())) {
                                            orderType = "Send Now";
                                        } else {
                                            orderType = "End of July";
                                        }
                                    } else if (transmissionMode.equalsIgnoreCase("XML")) {
                                        if (delivery.getAuthUntilDate() != null) {
                                            orderType = "Ongoing updates until " + delivery.getAuthUntilDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                        } else {
                                            orderType = "One-Time";
                                        }
                                    }
                                }
                                
                                List<String> row = prepareDataForCsv(student, psiName, transmissionMode, orderType);
                                csvPrinter.printRecord(row);
                            }
                        }
                    }
                } else {
                    psiName = "Not Applicable";
                    transmissionMode = "Not Applicable";
                    orderType = "Not Applicable";

                    List<String> row = prepareDataForCsv(student, psiName, transmissionMode, orderType);
                    csvPrinter.printRecord(row);
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
