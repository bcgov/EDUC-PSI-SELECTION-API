package ca.bc.gov.educ.psi.selection.api.service.v1;

import ca.bc.gov.educ.psi.selection.api.mapper.v1.PsiMapper;
import ca.bc.gov.educ.psi.selection.api.model.v1.sts.PsiEntity;
import ca.bc.gov.educ.psi.selection.api.repository.v1.PSIRepository;
import ca.bc.gov.educ.psi.selection.api.struct.v1.Psi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PSIService {
    private final PSIRepository psiRepository;
    private final PsiMapper psiMapper = PsiMapper.mapper;

    public PSIService(PSIRepository psiRepository) {
        this.psiRepository = psiRepository;
    }

    public List<Psi> getPsiEntities(String transmissionMode, String psiCode, String psiName, 
                                    String cslCode, String openFlag) {
        Specification<PsiEntity> spec = buildSpecification(transmissionMode, psiCode, psiName, cslCode, openFlag);
        List<PsiEntity> entities = psiRepository.findAll(spec);
        return entities.stream().map(psiMapper::toStructure).toList();
    }

    private Specification<PsiEntity> buildSpecification(String transmissionMode, String psiCode, String psiName, 
                                                        String cslCode, String openFlag) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.isNotBlank(transmissionMode)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("transmissionMode")), transmissionMode.toUpperCase() + "%"));
            }
            if (StringUtils.isNotBlank(psiCode)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("psiCode")), psiCode.toUpperCase() + "%"));
            }
            if (StringUtils.isNotBlank(psiName)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("psiName")), "%" + psiName.toUpperCase() + "%"));
            }
            if (StringUtils.isNotBlank(cslCode)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.upper(root.get("cslCode")), cslCode.toUpperCase() + "%"));
            }
            if (StringUtils.isNotBlank(openFlag)) {
                predicates.add(criteriaBuilder.equal(root.get("openFlag"), openFlag));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
