package pl.asap.asapbe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(exclude = {"tasks", "projects"})
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    @JsonIgnore
    private String password;

    @OneToMany(mappedBy = "assignee")
    @JsonIgnore
    private Set<TaskEntity> tasks;

    @ManyToMany(mappedBy = "users")
    @JsonIgnore
    private Set<ProjectEntity> projects;

    public UserEntity(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public UserEntity(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
}
