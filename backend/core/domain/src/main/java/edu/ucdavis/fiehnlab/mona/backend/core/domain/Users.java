package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@IdClass(UserId.class)
@Table(name = "users")
@Profile({"mona.persistence"})
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id")
    @SequenceGenerator(name = "users_id", initialValue = 1, allocationSize = 50)
    private Long id;

    @Id
    private String emailAddress;

    private String password;

    @OneToMany(fetch = FetchType.EAGER,  cascade = CascadeType.ALL)
    @JoinColumn(name = "users_id", referencedColumnName = "id")
    @JoinColumn(name = "users_email_address", referencedColumnName = "emailAddress")
    private List<Roles> roles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Roles> getRoles() {
        return roles;
    }

    public void setRoles(List<Roles> roles) {
        this.roles = roles;
    }

    public Users(Long id, String emailAddress, String password, List<Roles> roles) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.password = password;
        this.roles = roles;
    }

    public Users(String emailAddress, String password) {
        this.emailAddress = emailAddress;
        this.password = password;
    }

    public Users(String emailAddress, String password, List<Roles> roles) {
        this.emailAddress = emailAddress;
        this.password = password;
        this.roles = roles;
    }

    public Users(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Users users = (Users) o;
        return Objects.equals(id, users.id) && Objects.equals(emailAddress, users.emailAddress) && Objects.equals(password, users.password) && Objects.equals(roles, users.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, emailAddress, password, roles);
    }
}
