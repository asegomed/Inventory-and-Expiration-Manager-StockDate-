package stockdate.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stockdate.persistence.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
