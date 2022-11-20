package edu.ucdavis.fiehnlab.mona.backend.core.domain.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "submitters")
@Profile({"mona.persistence"})
public class SubmitterDAO implements Serializable {
    @Id
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String institution;

    public SubmitterDAO() {
    }

    public SubmitterDAO(String emailAddress, String firstName, String lastName, String institution) {
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.institution = institution;
    }

    public SubmitterDAO(SubmitterDAO submitter) {
        this.emailAddress = submitter.getEmailAddress();
        this.firstName = submitter.getFirstName();
        this.lastName = submitter.getFirstName();
        this.institution = submitter.getInstitution();
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getInstitution() {
        return institution;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubmitterDAO that = (SubmitterDAO) o;
        return Objects.equals(emailAddress, that.emailAddress) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(institution, that.institution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailAddress, firstName, lastName, institution);
    }
}
