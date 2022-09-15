package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import org.springframework.context.annotation.Profile;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@IdClass(SubmitterId.class)
@Table(name = "submitter")
@Profile({"mona.persistence"})
public class Submitter {
    @Id
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String institution;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Submitter submitter = (Submitter) o;
        return Objects.equals(emailAddress, submitter.emailAddress) && Objects.equals(firstName, submitter.firstName) && Objects.equals(lastName, submitter.lastName) && Objects.equals(institution, submitter.institution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailAddress, firstName, lastName, institution);
    }
}
