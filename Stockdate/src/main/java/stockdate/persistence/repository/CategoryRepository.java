package stockdate.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stockdate.persistence.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
}
