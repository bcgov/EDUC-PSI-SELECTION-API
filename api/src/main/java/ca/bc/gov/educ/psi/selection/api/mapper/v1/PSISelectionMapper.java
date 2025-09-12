package ca.bc.gov.educ.psi.selection.api.mapper.v1;

import ca.bc.gov.educ.psi.selection.api.mapper.LocalDateTimeMapper;
import ca.bc.gov.educ.psi.selection.api.mapper.StringMapper;
import ca.bc.gov.educ.psi.selection.api.mapper.UUIDMapper;
import ca.bc.gov.educ.psi.selection.api.model.v1.PSISelectionEntity;
import ca.bc.gov.educ.psi.selection.api.struct.v1.PSISelection;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class})
@SuppressWarnings("squid:S1214")
public interface PSISelectionMapper {

  PSISelectionMapper mapper = Mappers.getMapper(PSISelectionMapper.class);

  PSISelectionEntity toModel(PSISelection structure);

  PSISelection toStructure(PSISelectionEntity structure);

}
