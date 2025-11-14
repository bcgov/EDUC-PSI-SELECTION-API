package ca.bc.gov.educ.psi.selection.api.model.v1.sts;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ISD_STUD_PEN")
public class StudentPENEntity {
    @Id
    @Column(name = "STUD_PEN_ID")
    private String studentPenID;
    
    @Column(name = "VALUE")
    private String studentPen;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "studentPENEntity", fetch = FetchType.EAGER, cascade = CascadeType.DETACH, targetEntity = StudentXrefEntity.class)
    Set<StudentXrefEntity> studentXrefEntities;
}
