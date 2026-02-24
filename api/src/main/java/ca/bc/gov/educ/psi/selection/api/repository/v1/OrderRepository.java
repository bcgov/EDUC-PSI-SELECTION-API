package ca.bc.gov.educ.psi.selection.api.repository.v1;

import ca.bc.gov.educ.psi.selection.api.model.v1.sts.OrderEntity;
import ca.bc.gov.educ.psi.selection.api.struct.v1.PSIOrderRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {

    @Query("""
    SELECT new ca.bc.gov.educ.psi.selection.api.struct.v1.PSIOrderRow(
        pen.studentPen,
        di.infoType,
        di.transmissionMode,
        di.psiCode,
        di.authUntilDate,
        oi.ecmPsiMailBtcID
    )
    FROM OrderEntity o
    JOIN o.studentXrefEntities sx
    JOIN sx.studentPENEntity pen
    JOIN o.orderItemEntities oi
    JOIN oi.deliveryInfoEntity di
    WHERE pen.studentPen IN :pens
    AND o.createDate BETWEEN :start AND :end
    AND o.orderDate IS NOT NULL
    AND di.infoType = 'PSI_PREF'
    """)
    List<PSIOrderRow> findOrderRowsByStudentPensAndDateRange(
            @Param("pens") Set<String> pens,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
