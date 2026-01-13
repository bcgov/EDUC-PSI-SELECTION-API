package ca.bc.gov.educ.psi.selection.api.endpoint.v1;

import ca.bc.gov.educ.psi.selection.api.constants.v1.URL;
import ca.bc.gov.educ.psi.selection.api.struct.v1.DownloadableReportResponse;
import ca.bc.gov.educ.psi.selection.api.struct.v1.PSISelection;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@RequestMapping(URL.REPORT_URL)
public interface PSIReportEndpoint {
    @GetMapping("/school/{schoolID}")
    @PreAuthorize("hasAuthority('SCOPE_READ_PSI_SELECTION')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    @Transactional(readOnly = true)
    @Tag(name = "PSISelection Entity", description = "Endpoints for psi selection entity.")
    @Schema(name = "PSISelection", implementation = PSISelection.class)
    DownloadableReportResponse generatePSIReportForSchool(@PathVariable(value = "schoolID") UUID schoolID);

}
