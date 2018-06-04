package pl.asap.asapbe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.Set;

@Entity
public class ProjectEntity {

    @Id
    @GeneratedValue
    private Long id;
    private String title;

    @OneToOne
    private UserEntity supervisor;

    @ManyToMany
    private Set<UserEntity> users;

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    private Set<TaskEntity> tasks;

    public ProjectEntity() {}

    public ProjectEntity(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UserEntity getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(UserEntity supervisor) {
        this.supervisor = supervisor;
    }

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }

    public Set<TaskEntity> getTasks() {
        return tasks;
    }

    public void setTasks(Set<TaskEntity> tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return "ProjectEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", supervisor=" + supervisor +
                ", users=" + users +
                ", tasks=" + tasks +
                '}';
    }
}
