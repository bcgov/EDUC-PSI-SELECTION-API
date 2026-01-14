package ca.bc.gov.educ.psi.selection.api.mapper.v1;

import ca.bc.gov.educ.psi.selection.api.mapper.StringMapper;
import ca.bc.gov.educ.psi.selection.api.model.v1.sts.PsiEntity;
import ca.bc.gov.educ.psi.selection.api.struct.v1.Psi;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {StringMapper.class})
public interface PsiMapper {

    PsiMapper mapper = Mappers.getMapper(PsiMapper.class);

    Psi toStructure(PsiEntity entity);
}
