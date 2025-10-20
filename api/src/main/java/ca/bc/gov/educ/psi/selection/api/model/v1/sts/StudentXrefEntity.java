package ca.bc.gov.educ.psi.selection.api.model.v1.sts;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ISD_STUD_STUDENT_X_REF")
public class StudentXrefEntity {
    @Id
    @Column(name = "STUD_STUDENT_X_REF_ID")
    private String studentXRefId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false, targetEntity = OrderEntity.class)
    @JoinColumn(name = "USER_PROFILE_ENTITY_ID", referencedColumnName = "USERPROFILE_ENTITY_ID", updatable = false)
    OrderEntity orderEntity;

    @Column(name = "STUD_PEN_ID")
    private String studentPen;
    
}
