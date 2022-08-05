package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import javax.persistence.*;
import java.util.List;

@Entity
@IdClass(UserId.class)
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id")
    @SequenceGenerator(name = "users_id", initialValue = 1, allocationSize = 50)
    private Long id;

    @Id
    private String emailAddress;

    private String password;

    @OneToMany(fetch = FetchType.EAGER, targetEntity = Roles.class, cascade = CascadeType.ALL)
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

    /*@JoinTable(
            name = "users_roles",
            joinColumns = {@JoinColumn(name = "users_id", referencedColumnName = "id"), @JoinColumn(name = "users_email_address", referencedColumnName = "emailAddress")},
            inverseJoinColumns = {@JoinColumn(name = "roles_id", referencedColumnName = "id"), @JoinColumn(name = "roles_email_address", referencedColumnName = "emailAddress")}
    )*/
}
