package ca.bc.gov.educ.psi.selection.api.controller.v1;

import ca.bc.gov.educ.psi.selection.api.endpoint.v1.PSIEndpoint;
import ca.bc.gov.educ.psi.selection.api.service.v1.PSIService;
import ca.bc.gov.educ.psi.selection.api.struct.v1.Psi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class PSIController implements PSIEndpoint {
    
    private final PSIService psiService;

    public PSIController(PSIService psiService) {
        this.psiService = psiService;
    }

    @Override
    public ResponseEntity<List<Psi>> getPsiEntities(String transmissionMode, String psiCode, String psiName,
                                                     String cslCode, String openFlag) {
        return new ResponseEntity<>(psiService.getPsiEntities(transmissionMode, psiCode, psiName, cslCode, openFlag), HttpStatus.OK);
    }
}
