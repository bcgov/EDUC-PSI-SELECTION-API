package ca.bc.gov.educ.psi.selection.api.struct.v1;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class Psi {
    private String psiCode;
    private String psiName;
    private String address1;
    private String address2;
    private String address3;
    private String city;
    private String provinceCode;
    private String countryCode;
    private String postal;
    private String cslCode;
    private String attentionName;
    private String openFlag;
    private String fax;
    private String phone1;
    private String transmissionMode;
    private String psisCode;
    private String psiUrl;
    private String psiGrouping;
}
