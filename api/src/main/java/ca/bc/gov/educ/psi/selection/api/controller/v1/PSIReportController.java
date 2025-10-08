package ca.bc.gov.educ.psi.selection.api.controller.v1;

import ca.bc.gov.educ.psi.selection.api.endpoint.v1.PSIReportEndpoint;
import ca.bc.gov.educ.psi.selection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.psi.selection.api.rest.RestUtils;
import ca.bc.gov.educ.psi.selection.api.service.v1.PSIReportService;
import ca.bc.gov.educ.psi.selection.api.struct.v1.DownloadableReportResponse;
import ca.bc.gov.educ.psi.selection.api.struct.v1.external.institute.SchoolTombstone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class PSIReportController implements PSIReportEndpoint {
    private final PSIReportService pSIReportService;
    private final RestUtils restUtils;

    public PSIReportController(PSIReportService PSIReportService, RestUtils restUtils) {
        this.pSIReportService = PSIReportService;
        this.restUtils = restUtils;
    }

    @Override
    public DownloadableReportResponse generatePSIReportForSchool(UUID schoolID) {
        Optional<SchoolTombstone> optSchool = restUtils.getSchoolBySchoolID(schoolID.toString());
        if (optSchool.isEmpty()) {
            throw new EntityNotFoundException(SchoolTombstone.class, "schoolID", schoolID.toString());
        }
        return pSIReportService.generateReport(optSchool.get());
    }
}
