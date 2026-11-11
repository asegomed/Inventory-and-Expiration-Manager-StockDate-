package stockdate.persistence.repository;

import stockdate.persistence.entity.BatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<BatchEntity, Long> {

    List<BatchEntity> findAllByOrderByExpirationDateAscIdAsc();
}
