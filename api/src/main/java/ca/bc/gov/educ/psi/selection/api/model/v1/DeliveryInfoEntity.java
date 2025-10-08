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
@Table(name = "ECM_DLVRY_INF")
public class DeliveryInfoEntity {
    @Id
    @Column(name = "ECM_DLVRY_INF_ID")
    private String ecmDlvryInfId;

    @Column(name = "INFO_TYPE")
    private String infoType;

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

    @Column(name="AUTH_UNTIL_DATE")
    private String authUntilDate;

    @Column(name="INTERIM")
    private String interim;

    @Column(name="PSI_CODE")
    private String psiCode;

    @Column(name="NAME")
    private String name;

    @Column(name="CITY")
    private String city;

    @Column(name="COUNTRY_CD")
    private String countryCd;

    @Column(name="POSTALCODE")
    private String postalCode;

    @Column(name="PROVINCE")
    private String province;

    @Column(name="STREET_LINE_1")
    private String streetLine1;

    @Column(name="STREET_LINE_2")
    private String streetLine2;

    @Column(name="STREET_LINE_3")
    private String streetLine3;

    @Column(name="DOWNLOADED")
    private String downloaded;

    @Column(name="EMAILADDRESS")
    private String emailAddress;

    @Column(name="EXP_DATE")
    private String expDate;

    @Column(name="ATMPTS")
    private String attempts;

    @Column(name="ORDER_DATE")
    private String orderDate;

    @Column(name="RECIP_NAME")
    private String recipName;

    @Column(name="SEC_ANSER")
    private String secAnser;

    @Column(name="SEC_QUEST")
    private String secQuest;

    @Column(name="TRANSMISSION_MODE")
    private String transmissionMode;
}
