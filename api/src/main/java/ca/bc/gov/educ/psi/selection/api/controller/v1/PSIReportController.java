package ca.bc.gov.educ.psi.selection.api.controller.v1;

import ca.bc.gov.educ.psi.selection.api.endpoint.v1.PSIReportEndpoint;
import ca.bc.gov.educ.psi.selection.api.service.v1.PSIReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
public class PSIReportController implements PSIReportEndpoint {
    private final PSIReportService pSIReportService;

    public PSIReportController(PSIReportService PSIReportService) {
        this.pSIReportService = PSIReportService;
    }

    @Override
    public ResponseEntity<String> generatePSIReportForSchool(UUID schoolID) {
        return new ResponseEntity(pSIReportService.generateReport(schoolID), HttpStatus.OK);
    }
}
