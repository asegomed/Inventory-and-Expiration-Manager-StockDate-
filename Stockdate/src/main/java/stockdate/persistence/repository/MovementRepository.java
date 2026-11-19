package stockdate.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stockdate.persistence.entity.StockMovementEntity;

public interface MovementRepository extends JpaRepository<StockMovementEntity, Long> {
}
