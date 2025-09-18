package ca.bc.gov.educ.psi.selection.api.service.v1;

import ca.bc.gov.educ.psi.selection.api.rest.RestUtils;
import ca.bc.gov.educ.psi.selection.api.repository.v1.StudentPSIChoiceRepository;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.student.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PSIReportService {
    private final StudentPSIChoiceRepository studentPSIChoiceRepository;
    private final RestUtils restUtils;

    public PSIReportService(StudentPSIChoiceRepository studentPSIChoiceRepository, RestUtils restUtils) {
        this.studentPSIChoiceRepository = studentPSIChoiceRepository;
        this.restUtils = restUtils;
    }

    public String generateReport(UUID schoolID) {
        // Send a schoolID to grad student -> receive UUID of students based on criteria
        List<UUID> studentGradRecordIDs = restUtils.getGradStudentUUIDsFromSchoolID(schoolID);
        log.debug("studentGradRecordIDs: {}", studentGradRecordIDs);

        // Get student details from student API for each studentGradRecordID UUID
        List<Student> students = restUtils.getStudentDetailsByUUIDs(studentGradRecordIDs);
        log.debug("Fetched {} student details", students.size());

        // todo get psi data for students (create records in dev db)

        // todo build csv with row per student

        return "Report generated successfully.";
    }
}
