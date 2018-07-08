package pl.asap.asapbe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(exclude = {"assignee", "project"})
@NoArgsConstructor
public class TaskEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity assignee;

    @ManyToOne
    @JsonIgnore
    private ProjectEntity project;


    public TaskEntity(String title, String description, Status status, Priority priority) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
    }
}