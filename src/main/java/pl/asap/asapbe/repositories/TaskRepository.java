package pl.asap.asapbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.asap.asapbe.entities.TaskEntity;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
}
