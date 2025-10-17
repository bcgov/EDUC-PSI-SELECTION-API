package ca.bc.gov.educ.psi.selection.api.model.v1.sts;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ECM_SLS_ORDR")
public class OrderEntity {

    @Id
    @Column(name = "ECM_SLS_ORDR_ID")
    private String orderID;

    @Column(name = "ORDER_STATUS")
    private String orderStatus;

    @Column(name = "USERPROFILE_ENTITY_ID")
    private String userProfileEntityID;

    @Column(name = "ORDER_DATE")
    private LocalDateTime orderDate;

    @Column(name = "CREATE_DTTM", updatable = false)
    private LocalDateTime createDate;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "orderEntity", fetch = FetchType.EAGER, cascade = CascadeType.DETACH, targetEntity = OrderItemEntity.class)
    Set<OrderItemEntity> orderItemEntities;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "orderEntity", fetch = FetchType.EAGER, cascade = CascadeType.DETACH, targetEntity = StudentXrefEntity.class)
    Set<StudentXrefEntity> studentXrefEntities;
}
