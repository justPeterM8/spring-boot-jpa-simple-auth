package pl.asap.asapbe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(exclude = {"users", "tasks", "supervisor"})
@NoArgsConstructor
public class ProjectEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String title;

    @OneToOne
    @JsonIgnore
    private UserEntity supervisor;

    @ManyToMany
    @JsonIgnore
    private Set<UserEntity> users;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<TaskEntity> tasks;

    public ProjectEntity(String title) {
        this.title = title;
    }
}
