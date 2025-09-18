package ca.bc.gov.educ.psi.selection.api.service.v1;

import ca.bc.gov.educ.psi.selection.api.exception.PSISelectionAPIRuntimeException;
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
import java.util.UUID;

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
        List<UUID> studentGradRecordIDs = restUtils.getGradStudentUUIDsFromSchoolID(UUID.fromString(school.getSchoolId()));
        log.debug("studentGradRecordIDs: {}", studentGradRecordIDs);

        // Get student details from student API for each studentGradRecordID UUID
        List<Student> students = restUtils.getStudentDetailsByUUIDs(studentGradRecordIDs);
        log.debug("Fetched {} student details", students.size());
        log.debug("first fetched student if available: {}", students.stream().findFirst().orElse(null));

        // todo get psi data for students (create records in dev db)
        // what is the current school year dates exactly?
        List<StudentPsiChoiceEntity> studentPsiChoiceEntities = studentPSIChoiceRepository.findStudentsInAllPSIs("PAPER", LocalDate.now().withMonth(7).withDayOfMonth(1).atStartOfDay(), LocalDate.now().withMonth(9).withDayOfMonth(27).atStartOfDay());
        log.debug("Fetched {} student PSI choice records", studentPsiChoiceEntities.size());
        log.debug("first fetched student PSI choice record if available: {}", studentPsiChoiceEntities.stream().findFirst().orElse(null));

        // todo update csv content with psi choice

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(SURNAME.getCode(), FIRST_NAME.getCode(), MIDDLE_NAMES.getCode(), LOCAL_ID.getCode(), PEN.getCode(), PSI_REPORT.getCode(), TRANSMISSION_MODE.getCode())
                .build();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

            for (Student student : students) {
                List<String> row = prepareErrorDataForCsv(student);
                csvPrinter.printRecord(row);
            }

            csvPrinter.flush();

            var downloadableReport = new DownloadableReportResponse();
            downloadableReport.setReportType(PSI_REPORT.getCode());
            downloadableReport.setReportName(String.format("%s - PSI Report - %s", school.getMincode(), LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
            downloadableReport.setDocumentData(Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));
            return downloadableReport;

        } catch (IOException e) {
            throw new PSISelectionAPIRuntimeException(e.toString());
        }
    }

    public List<String> prepareErrorDataForCsv(Student student) {
        return Arrays.asList(
                        student.getLegalLastName(),
                        student.getLegalFirstName(),
                        student.getLegalMiddleNames(),
                        student.getLocalID(),
                        student.getPen(),
                        "", // PSI
                        ""  // Transmission Mode
                );
    }
}
