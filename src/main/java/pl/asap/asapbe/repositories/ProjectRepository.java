package pl.asap.asapbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.asap.asapbe.entities.ProjectEntity;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    ProjectEntity findByTitle(String title);
}
