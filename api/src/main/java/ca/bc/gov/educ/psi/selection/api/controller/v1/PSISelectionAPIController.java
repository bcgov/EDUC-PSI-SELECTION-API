package ca.bc.gov.educ.psi.selection.api.controller.v1;

import ca.bc.gov.educ.psi.selection.api.endpoint.v1.PSISelectionAPIEndpoint;
import ca.bc.gov.educ.psi.selection.api.mapper.v1.PSISelectionMapper;
import ca.bc.gov.educ.psi.selection.api.service.v1.PSISelectionService;
import ca.bc.gov.educ.psi.selection.api.struct.v1.PSISelection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Slf4j
public class PSISelectionAPIController implements PSISelectionAPIEndpoint {
  
  private static final PSISelectionMapper mapper = PSISelectionMapper.mapper;

  private final PSISelectionService psiSelectionService;

  public PSISelectionAPIController(PSISelectionService PSISelectionService) {
      this.psiSelectionService = PSISelectionService;
  }

  @Override
  public PSISelection getPSISelection(UUID psiSelectionID) {
    return mapper.toStructure(psiSelectionService.getPSISelection(psiSelectionID));
  }
}
