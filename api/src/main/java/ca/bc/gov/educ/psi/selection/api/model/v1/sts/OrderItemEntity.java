package ca.bc.gov.educ.psi.selection.api.model.v1.sts;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ECM_SLS_ORDR_ITM")
public class OrderItemEntity {
    @Id
    @Column(name = "ECM_SLS_ORDR_ITM_ID")
    private String orderItemID;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false, targetEntity = OrderEntity.class)
    @JoinColumn(name = "ECM_SLS_ORDR_ID", referencedColumnName = "ECM_SLS_ORDR_ID", updatable = false)
    OrderEntity orderEntity;

    @Column(name = "ORDER_ITM_STATUS")
    private String orderItemStatus;

    @Column(name = "ECM_PSI_MAIL_BTC_ID")
    private String ecmPsiMailBtcID;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false, targetEntity = DeliveryInfoEntity.class)
    @JoinColumn(name = "ECM_DLVRY_INF_ID", referencedColumnName = "ECM_DLVRY_INF_ID", updatable = false)
    DeliveryInfoEntity deliveryInfoEntity;
}
