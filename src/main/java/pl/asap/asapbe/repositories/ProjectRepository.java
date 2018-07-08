package pl.asap.asapbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.asap.asapbe.entities.ProjectEntity;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    Optional<ProjectEntity> findByTitle(String title);
}
