package ca.bc.gov.educ.psi.selection.api.controller.v1;

import ca.bc.gov.educ.psi.selection.api.endpoint.v1.PSISelectionAPIEndpoint;
import ca.bc.gov.educ.psi.selection.api.mapper.v1.StudentPSIChoiceMapper;
import ca.bc.gov.educ.psi.selection.api.service.v1.PSISelectionService;
import ca.bc.gov.educ.psi.selection.api.struct.v1.StudentPsiChoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class PSISelectionAPIController implements PSISelectionAPIEndpoint {
  
  private static final StudentPSIChoiceMapper mapper = StudentPSIChoiceMapper.mapper;

  private final PSISelectionService psiSelectionService;

  public PSISelectionAPIController(PSISelectionService PSISelectionService) {
      this.psiSelectionService = PSISelectionService;
  }

  @Override
  public ResponseEntity<List<StudentPsiChoice>> getStudentPSIDetails(String transmissionMode, String psiCode, String psiYear) {
    return new ResponseEntity(psiSelectionService.getStudentPSIChoices(transmissionMode, psiYear, psiCode), HttpStatus.OK);
  }
}
