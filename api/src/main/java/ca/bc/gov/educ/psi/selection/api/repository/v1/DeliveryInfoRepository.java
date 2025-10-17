package ca.bc.gov.educ.psi.selection.api.repository.v1;

import ca.bc.gov.educ.psi.selection.api.model.v1.sts.DeliveryInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryInfoRepository extends JpaRepository<DeliveryInfoEntity, UUID>, JpaSpecificationExecutor<DeliveryInfoEntity> {
}
