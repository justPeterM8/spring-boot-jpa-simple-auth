package pl.asap.asapbe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
@EqualsAndHashCode(exclude = {"assignee", "project"})
@NoArgsConstructor
public class UserAuthDetailsEntity {

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;
    private Long userId;
    private String token;

    public UserAuthDetailsEntity(String token) {
        this.token = token;
    }

    public UserAuthDetailsEntity(Long userId, String token) {
        this.token = token;
        this.userId = userId;
    }
}
