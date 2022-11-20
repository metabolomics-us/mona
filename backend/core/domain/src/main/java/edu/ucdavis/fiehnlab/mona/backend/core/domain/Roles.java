package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "roles")
@Profile({"mona.persistence"})
public class Roles {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roles_id")
    @SequenceGenerator(name = "roles_id", initialValue = 1, allocationSize = 50)
    private Long id;

    @ManyToOne
    @JsonIgnore
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Roles roles = (Roles) o;
        return Objects.equals(id, roles.id) && Objects.equals(users, roles.users) && Objects.equals(name, roles.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, users, name);
    }
}
