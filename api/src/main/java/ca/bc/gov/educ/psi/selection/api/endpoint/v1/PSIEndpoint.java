package ca.bc.gov.educ.psi.selection.api.endpoint.v1;

import ca.bc.gov.educ.psi.selection.api.constants.v1.URL;
import ca.bc.gov.educ.psi.selection.api.struct.v1.Psi;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping(URL.PSI_URL)
public interface PSIEndpoint {

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SCOPE_READ_PSI')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    @Transactional(readOnly = true)
    @Tag(name = "PSI Entity", description = "Endpoints for PSI entity.")
    @Schema(name = "Psi", implementation = Psi.class)
    ResponseEntity<List<Psi>> getPsiEntities(
            @RequestParam(value = "transmissionMode", required = false) String transmissionMode,
            @RequestParam(value = "psiCode", required = false) String psiCode,
            @RequestParam(value = "psiName", required = false) String psiName,
            @RequestParam(value = "cslCode", required = false) String cslCode,
            @RequestParam(value = "openFlag", required = false) String openFlag);
}
