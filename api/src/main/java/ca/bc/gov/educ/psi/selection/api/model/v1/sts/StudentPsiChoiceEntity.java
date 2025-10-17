package ca.bc.gov.educ.psi.selection.api.model.v1.sts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ISD_PSI_CHOICES")
public class StudentPsiChoiceEntity {
  @Id
  @Column(name = "PSI_CHOICES_ID")
  private BigInteger psiChoicesID;
  
  @Column(name = "STUD_NO")
  private String pen;

  @Column(name = "PSI_CODE")
  private String psiCode;

  @Column(name = "STATUS")
  private String status;

  @Column(name = "ENTITY_ID")
  private String entityID;

  @Column(name = "SYNC_DATE")
  private LocalDateTime syncDate;

  @Column(name = "XACTID")
  private Integer xactID;

  @Column(name = "CREATE_USER", updatable = false)
  String createUser;
  
  @Column(name = "CREATE_DTTM", updatable = false)
  LocalDateTime createDate;

  @Column(name = "UPDATE_USER")
  String updateUser;

  @Column(name = "UPDATE_DTTM")
  LocalDateTime updateDate;

}
