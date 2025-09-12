package ca.bc.gov.educ.psi.selection.api.service.v1;

import ca.bc.gov.educ.psi.selection.api.exception.EntityNotFoundException;
import ca.bc.gov.educ.psi.selection.api.model.v1.PSISelectionEntity;
import ca.bc.gov.educ.psi.selection.api.repository.v1.PSISelectionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class PSISelectionService {

  private static final String PSI_SELECTION_ID_ATTR = "psiSelectionID";

  private final PSISelectionRepository psiSelectionRepository;

  public PSISelectionService(PSISelectionRepository psiSelectionRepository) {
      this.psiSelectionRepository = psiSelectionRepository;
  }
  
  public PSISelectionEntity getPSISelection(UUID psiSelectionID) {
    return psiSelectionRepository.findById(psiSelectionID).orElseThrow(
            () -> new EntityNotFoundException(PSISelectionEntity.class, PSI_SELECTION_ID_ATTR, psiSelectionID.toString()));
  }

}
