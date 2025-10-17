package ca.bc.gov.educ.psi.selection.api.model.v1.sts;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false, targetEntity = OrderItemEntity.class)
    @JoinColumn(name = "ECM_DLVRY_INF_ID", referencedColumnName = "ECM_DLVRY_INF_ID", updatable = false)
    OrderItemEntity orderItemEntity;

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
