package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
@IdClass(SubmitterId.class)
@Table(name = "submitter")
public class Submitter {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "submitter_gen_id")
    @SequenceGenerator(name = "submitter_gen_id", initialValue = 1, allocationSize = 50)
    private Long id;
    @Id
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String institution;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public Submitter() {}

    public Submitter(String emailAddress, String firstName, String lastName, String institution) {
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = firstName;
        this.institution = institution;
    }
}
