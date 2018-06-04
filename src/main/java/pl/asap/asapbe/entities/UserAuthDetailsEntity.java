package pl.asap.asapbe.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class UserAuthDetailsEntity {

    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;
    private Long userId;
    private String token;

    public UserAuthDetailsEntity() {}

    public UserAuthDetailsEntity(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserAuthDetailsEntity(Long userId, String token) {
        this.token = token;
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
