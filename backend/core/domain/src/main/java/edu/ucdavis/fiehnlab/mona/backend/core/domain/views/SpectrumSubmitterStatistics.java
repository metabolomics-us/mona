package edu.ucdavis.fiehnlab.mona.backend.core.domain.views;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.util.Objects;

@Entity
@IdClass(SpectrumSubmitterStatisticsId.class)
@Subselect("select * from spectrum_submitter_statistics")
@Immutable
public class SpectrumSubmitterStatistics {
    @Id
    private String id;

    private String lastName;

    private String firstName;

    private String institution;

    @Id
    private String emailAddress;

    private Double score;

    public SpectrumSubmitterStatistics() {
    }

    public SpectrumSubmitterStatistics(String id, String lastName, String firstName, String institution, String emailAddress, Double score) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.institution = institution;
        this.emailAddress = emailAddress;
        this.score = score;
    }

    public String getId() {
        return id;
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
        SpectrumSubmitterStatistics that = (SpectrumSubmitterStatistics) o;
        return Objects.equals(id, that.id) && Objects.equals(lastName, that.lastName) && Objects.equals(firstName, that.firstName) && Objects.equals(institution, that.institution) && Objects.equals(emailAddress, that.emailAddress) && Objects.equals(score, that.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastName, firstName, institution, emailAddress, score);
    }
}
