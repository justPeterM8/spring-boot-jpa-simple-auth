package pl.asap.asapbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.asap.asapbe.entities.UserAuthDetailsEntity;

public interface UserAuthDetailsRepository extends JpaRepository<UserAuthDetailsEntity, Long> {
    UserAuthDetailsEntity findByUserId(Long userId);
    UserAuthDetailsEntity findByToken(String authToken);
}
