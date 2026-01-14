package ca.bc.gov.educ.psi.selection.api.controller.v1;

import ca.bc.gov.educ.psi.selection.api.endpoint.v1.PSIEndpoint;
import ca.bc.gov.educ.psi.selection.api.mapper.v1.PsiMapper;
import ca.bc.gov.educ.psi.selection.api.service.v1.PSISearchService;
import ca.bc.gov.educ.psi.selection.api.struct.v1.Psi;
import ca.bc.gov.educ.psi.selection.api.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
public class PSIController implements PSIEndpoint {

    private final PSISearchService searchService;
    private static final PsiMapper mapper = PsiMapper.mapper;

    public PSIController(PSISearchService searchService) {
        this.searchService = searchService;
    }

    @Override
    public CompletableFuture<Page<Psi>> findAll(Integer pageNumber, Integer pageSize, String sortCriteriaJson, String searchCriteriaListJson) {
        final List<Sort.Order> sorts = new ArrayList<>();
        Specification<ca.bc.gov.educ.psi.selection.api.model.v1.sts.PsiEntity> specs = searchService
            .setSpecificationAndSortCriteria(
                sortCriteriaJson,
                searchCriteriaListJson,
                JsonUtil.mapper,
                sorts
            );
        return this.searchService
            .findAll(specs, pageNumber, pageSize, sorts)
            .thenApplyAsync(psiEntities -> psiEntities.map(mapper::toStructure));
    }
}