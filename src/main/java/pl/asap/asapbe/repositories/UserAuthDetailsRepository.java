package pl.asap.asapbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;

import java.util.Optional;

public interface UserAuthDetailsRepository extends JpaRepository<UserAuthDetailsEntity, Long> {
    Optional<UserAuthDetailsEntity> findByUserId(Long userId);
    Optional<UserAuthDetailsEntity> findByToken(String authToken);
}
