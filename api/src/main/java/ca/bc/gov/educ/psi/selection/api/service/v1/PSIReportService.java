package ca.bc.gov.educ.psi.selection.api.service.v1;

import ca.bc.gov.educ.psi.selection.api.exception.PSISelectionAPIRuntimeException;
import ca.bc.gov.educ.psi.selection.api.model.v1.PsiEntity;
import ca.bc.gov.educ.psi.selection.api.model.v1.StudentPsiChoiceEntity;
import ca.bc.gov.educ.psi.selection.api.rest.RestUtils;
import ca.bc.gov.educ.psi.selection.api.repository.v1.StudentPSIChoiceRepository;
import ca.bc.gov.educ.psi.selection.api.struct.v1.DownloadableReportResponse;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.institute.SchoolTombstone;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.student.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.psi.selection.api.constants.v1.reports.ReportTypeCodes.PSI_REPORT;
import static ca.bc.gov.educ.psi.selection.api.constants.v1.reports.PSIReportHeaders.*;

@Service
@Slf4j
public class PSIReportService {
    private final StudentPSIChoiceRepository studentPSIChoiceRepository;
    private final RestUtils restUtils;

    public PSIReportService(StudentPSIChoiceRepository studentPSIChoiceRepository, RestUtils restUtils) {
        this.studentPSIChoiceRepository = studentPSIChoiceRepository;
        this.restUtils = restUtils;
    }

    public DownloadableReportResponse generateReport(SchoolTombstone school) {
        // Send a schoolID to grad student -> receive UUID of students based on criteria (currently just schoolID)
        // todo need to put what is actually current in list of grad programs
        List<UUID> studentGradRecordIDs = restUtils.getGradStudentUUIDsFromSchoolID(List.of(UUID.fromString(school.getSchoolId())), List.of("1950", "SCCP"), List.of("12", "AD"));
        log.debug("studentGradRecordIDs: {}", studentGradRecordIDs);

        // Get student details from student API for each studentGradRecordID UUID
        List<Student> students = restUtils.getStudentDetailsByUUIDs(studentGradRecordIDs);
        log.debug("Fetched {} student details", students.size());
        log.debug("first fetched student if available: {}", students.stream().findFirst().orElse(null));

        // todo what is the current school year dates exactly?
        // oct 1st 2024 -> Sept 30 2025
        // do date logic - or grab from gdc frontend
        List<StudentPsiChoiceEntity> studentPsiChoiceEntities = studentPSIChoiceRepository.findStudentsInAllPSIs("PAPER", LocalDate.now().withMonth(7).withDayOfMonth(1).atStartOfDay(), LocalDate.now().withMonth(9).withDayOfMonth(27).atStartOfDay());
        log.debug("Fetched {} student PSI choice records", studentPsiChoiceEntities.size());
        log.debug("first fetched student PSI choice record if available: {}", studentPsiChoiceEntities.stream().findFirst().orElse(null));

        //todo from entity id on student psi choice pull in the order information from the tables below
//        SELECT * FROM ECM_SLS_ORDR_ITM;
//
//        SELECT * FROM ECM_DLVRY_INF c
//        WHERE c.info_type = 'PSI_PREF';

        // todo order item can be bound on entity_id to psi choice entity
        // todo delivery info can be bound on ecmDlvryInfId from order item to devlivery info table


        // Group PSI choices by PEN (student number) to handle multiple choices per student
        Map<String, List<StudentPsiChoiceEntity>> psiChoicesByPen = studentPsiChoiceEntities.stream()
                .filter(choice -> choice.getPen() != null)
                .collect(Collectors.groupingBy(StudentPsiChoiceEntity::getPen));

        log.debug("Grouped PSI choices by PEN for {} unique students", psiChoicesByPen.size());

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(SURNAME.getCode(), FIRST_NAME.getCode(), MIDDLE_NAMES.getCode(), LOCAL_ID.getCode(), PEN.getCode(), PSI_REPORT.getCode(), TRANSMISSION_MODE.getCode())
                .build();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

            for (Student student : students) {
                List<StudentPsiChoiceEntity> studentPsiChoices = psiChoicesByPen.getOrDefault(student.getPen(), List.of());
                List<String> row = prepareDataForCsv(student, studentPsiChoices);
                csvPrinter.printRecord(row);

                // Debug: Print row for the dev test student
                if ("143244747".equals(student.getPen())) {
                    log.debug("CSV row for student with PEN 143244747: {}", row);
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

    public List<String> prepareDataForCsv(Student student, List<StudentPsiChoiceEntity> psiChoices) {
        String psiNames = psiChoices.stream()
                .map(choice -> restUtils.getPsiByCode(choice.getPsiCode())
                        .map(PsiEntity::getPsiName)
                        .orElse("Unknown PSI"))
                .collect(Collectors.joining(";"));

        String transmissionModes = psiChoices.stream()
                .map(choice -> restUtils.getPsiByCode(choice.getPsiCode())
                        .map(PsiEntity::getTransmissionMode)
                        .orElse(""))
                .collect(Collectors.joining(";"));

        return Arrays.asList(
                student.getLegalLastName(),
                student.getLegalFirstName(),
                student.getLegalMiddleNames(),
                student.getLocalID(),
                student.getPen(),
                psiNames,
                transmissionModes
        );
    }
}
