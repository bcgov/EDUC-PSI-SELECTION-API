package ca.bc.gov.educ.psi.selection.api.struct.v1;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class StudentPsiChoice {

	private String pen;
	private String psiCode;
    private String psiYear;
}
