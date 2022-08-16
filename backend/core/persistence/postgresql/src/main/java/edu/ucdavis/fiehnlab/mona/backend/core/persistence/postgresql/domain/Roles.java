package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "roles")
@Profile({"mona.persistence"})
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roles_id")
    @SequenceGenerator(name = "roles_id", initialValue = 1, allocationSize = 50)
    private Long id;

    @ManyToOne
    private Users users;

    private String name;

    public Roles(String name) {
        this.name = name;
    }

    public Roles(){}

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
