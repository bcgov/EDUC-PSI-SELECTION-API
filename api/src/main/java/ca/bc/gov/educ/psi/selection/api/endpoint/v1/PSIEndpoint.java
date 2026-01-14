package ca.bc.gov.educ.psi.selection.api.endpoint.v1;

import ca.bc.gov.educ.psi.selection.api.constants.v1.URL;
import ca.bc.gov.educ.psi.selection.api.struct.v1.Psi;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.CompletableFuture;

@RequestMapping(URL.PSI_URL)
public interface PSIEndpoint {

    @GetMapping(URL.PAGINATED)
    @PreAuthorize("hasAuthority('SCOPE_READ_PSI')")
    @Transactional(readOnly = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
    @Tag(name = "PSI Entity", description = "Paginated endpoint for PSI entity.")
    CompletableFuture<Page<Psi>> findAll(
        @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
        @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson);
}