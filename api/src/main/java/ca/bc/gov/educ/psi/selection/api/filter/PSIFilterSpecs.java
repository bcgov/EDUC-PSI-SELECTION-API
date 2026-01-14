package ca.bc.gov.educ.psi.selection.api.filter;

import ca.bc.gov.educ.psi.selection.api.model.v1.sts.PsiEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PSIFilterSpecs extends BaseFilterSpecs<PsiEntity> {

  public PSIFilterSpecs(FilterSpecifications<PsiEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<PsiEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<PsiEntity, Integer> integerFilterSpecifications, FilterSpecifications<PsiEntity, String> stringFilterSpecifications, FilterSpecifications<PsiEntity, Long> longFilterSpecifications, FilterSpecifications<PsiEntity, UUID> uuidFilterSpecifications, FilterSpecifications<PsiEntity, Boolean> booleanFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, booleanFilterSpecifications, converters);
  }
}