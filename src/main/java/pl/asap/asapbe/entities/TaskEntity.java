package pl.asap.asapbe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

@Entity
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

    public enum Status {
        OPEN("OPEN"),
        IN_PROGRESS("IN_PROGRESS"),
        IN_TESTS("IN_TESTS"),
        DONE("DONE");

        private String statusName;

        Status(String statusName) {
            this.statusName = statusName;
        }

        public String getStatusName() {
            return statusName;
        }
    }

    public enum Priority {
        LOW("LOW"),
        NORMAL("NORMAL"),
        HIGH("HIGH");

        private String priorityName;

        Priority(String priorityName) {
            this.priorityName = priorityName;
        }

        public String getPriorityName() {
            return priorityName;
        }
    }

    public TaskEntity() {
    }

    public TaskEntity(String title, String description, Status status, Priority priority) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public UserEntity getAssignee() {
        return assignee;
    }

    public void setAssignee(UserEntity assignee) {
        this.assignee = assignee;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", user=" + assignee +
                '}';
    }
}