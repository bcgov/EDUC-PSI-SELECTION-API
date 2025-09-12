package ca.bc.gov.educ.psi.selection.api.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PSISelection extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 1L;

  private String psiSelectionID;

}
