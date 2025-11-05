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
@Table(name = "ECM_DLVRY_INF")
public class DeliveryInfoEntity {
    @Id
    @Column(name = "ECM_DLVRY_INF_ID")
    private String ecmDlvryInfId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "deliveryInfoEntity", fetch = FetchType.EAGER, cascade = CascadeType.DETACH, targetEntity = OrderItemEntity.class)
    Set<OrderItemEntity> orderItemEntities;

    @Column(name = "INFO_TYPE")
    private String infoType;

    @Column(name="AUTH_UNTIL_DATE")
    private LocalDateTime authUntilDate;

    @Column(name="INTERIM")
    private String interim;

    @Column(name="PSI_CODE")
    private String psiCode;
    
    @Column(name="TRANSMISSION_MODE")
    private String transmissionMode;
}
