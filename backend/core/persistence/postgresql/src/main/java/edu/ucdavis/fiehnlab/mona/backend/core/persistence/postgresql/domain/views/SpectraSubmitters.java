package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.util.Objects;

@Entity
@IdClass(SpectraSubmittersId.class)
@Subselect("select * from spectra_submitters")
@Immutable
public class SpectraSubmitters {
    @Id
    private String monaId;

    private String submitterId;

    private String lastName;

    private String firstName;

    private String institution;

    @Id
    private String emailAddress;

    private Double score;

    public SpectraSubmitters() {
    }

    public SpectraSubmitters(String monaId, String submitterId, String lastName, String firstName, String institution, String emailAddress, Double score) {
        this.monaId = monaId;
        this.submitterId = submitterId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.institution = institution;
        this.emailAddress = emailAddress;
        this.score = score;
    }

    public String getMonaId() {
        return monaId;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getInstitution() {
        return institution;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public Double getScore() {return score;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpectraSubmitters that = (SpectraSubmitters) o;
        return Objects.equals(monaId, that.monaId) && Objects.equals(submitterId, that.submitterId) && Objects.equals(lastName, that.lastName) && Objects.equals(firstName, that.firstName) && Objects.equals(institution, that.institution) && Objects.equals(emailAddress, that.emailAddress) && Objects.equals(score, that.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monaId, submitterId, lastName, firstName, institution, emailAddress, score);
    }
}
