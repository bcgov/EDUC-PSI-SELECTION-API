package ca.bc.gov.educ.psi.selection.api.model.v1;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "CREATE_USER")
    private String createUser;

    @Column(name = "CREATE_DTTM")
    private String createDate;

    @Column(name = "ENTITY_ID")
    private String entityID;

    @Column(name = "UPDATE_USER")
    private String updateUser;

    @Column(name = "UPDATE_DTTM")
    private String updateDate;

    @Column(name = "XACTID")
    private String xactID;

    @Column(name = "ENT_QUANTITY")
    private String entQuntity;

    @Column(name = "EXT_PRICE")
    private String extPrice;

    @Column(name = "QUANTITY")
    private String quantity;

    @Column(name = "ORDER_ITM_STATUS")
    private String orderItemStatus;

    @Column(name = "CATITEM_ECM_CAT_ITEM_ID")
    private String catItemEcmCatItemID;

    @Column(name = "ECM_DLVRY_INF_ID")
    private String ecmDlvryInfID;

    @Column(name = "ECM_PRCH_DOC_ID")
    private String ecmPrchDocID;

    @Column(name = "ECM_SLS_ORDR_ID")
    private String ecmSlsOrdrID;

    @Column(name = "ECM_PSI_MAIL_BTC_ID")
    private String ecmPsiMailBtcID;
}
