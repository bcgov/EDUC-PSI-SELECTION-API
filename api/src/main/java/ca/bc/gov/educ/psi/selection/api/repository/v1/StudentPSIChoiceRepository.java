package ca.bc.gov.educ.psi.selection.api.repository.v1;

import ca.bc.gov.educ.psi.selection.api.model.v1.StudentPsiChoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StudentPSIChoiceRepository extends JpaRepository<StudentPsiChoiceEntity, UUID>, JpaSpecificationExecutor<StudentPsiChoiceEntity> {

    @Query(value = "SELECT sp.* FROM ISD_PSI_CHOICES sp " +
            "INNER JOIN ISD_PSI_REGISTRY tab ON tab.PSI_CODE=sp.PSI_CODE " +
            "WHERE tab.TRANSMISSION_MODE = :transmissionMode " +
            "AND sp.CREATE_DTTM between :fromDate and :toDate " +
            "AND (tab.PSI_CODE in :psiCode)",nativeQuery = true)
    List<StudentPsiChoiceEntity> findStudentsUsingPSI(String transmissionMode, List<String> psiCode, LocalDateTime fromDate, LocalDateTime toDate);

    @Query(value = "SELECT sp.* FROM ISD_PSI_CHOICES sp " +
            "INNER JOIN ISD_PSI_REGISTRY tab ON tab.PSI_CODE=sp.PSI_CODE " +
            "WHERE tab.TRANSMISSION_MODE = :transmissionMode " +
            "AND sp.CREATE_DTTM between :fromDate and :toDate ",nativeQuery = true)
    List<StudentPsiChoiceEntity> findStudentsInAllPSIs(String transmissionMode, LocalDateTime fromDate, LocalDateTime toDate);
}
