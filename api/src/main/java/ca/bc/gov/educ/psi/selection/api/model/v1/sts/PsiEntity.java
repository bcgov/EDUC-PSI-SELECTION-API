package ca.bc.gov.educ.psi.selection.api.model.v1.sts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ISD_PSI_REGISTRY")
public class PsiEntity {
  @Id
  @Column(name = "PSI_CODE")
  private String psiCode;

  @Column(name = "PSI_NAME")
  private String psiName;

  @Column(name = "ADDRESS1")
  private String address1;

  @Column(name = "ADDRESS2")
  private String address2;

  @Column(name = "ADDRESS3")
  private String address3;

  @Column(name = "CITY")
  private String city;

  @Column(name = "PROV_CODE")
  private String provinceCode;

  @Column(name = "CNTRY_CODE")
  private String countryCode;

  @Column(name = "PSI_POSTAL")
  private String postal;

  @Column(name = "PSI_CSL_CODE")
  private String cslCode;

  @Column(name = "PSI_ATTN_NAME")
  private String attentionName;

  @Column(name = "OPEN_FLAG")
  private String openFlag;

  @Column(name = "FAX")
  private String fax;

  @Column(name = "PHONE1")
  private String phone1;

  @Column(name = "TRANSMISSION_MODE")
  private String transmissionMode;

  @Column(name = "PSIS_CODE")
  private String psisCode;

  @Column(name = "PSI_URL")
  private String psiUrl;

  @Column(name = "PSI_GROUPING")
  private String psiGrouping;

}
