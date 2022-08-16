package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics;

import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(StatisticsSubmitterId.class)
@Table(name = "statistics_submitters")
@Profile({"mona.persistence"})
public class StatisticsSubmitter {
    @Id
    private String emailAddress;

    private String firstName;

    private String lastName;

    private String institution;

    private Integer count;

    private Double score;

    public StatisticsSubmitter() {
    }

    public StatisticsSubmitter(String emailAddress, String firstName, String lastName, String institution, Integer count, Double score) {
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.institution = institution;
        this.count = count;
        this.score = score;
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

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticsSubmitter that = (StatisticsSubmitter) o;
        return Objects.equals(emailAddress, that.emailAddress) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(institution, that.institution) && Objects.equals(count, that.count) && Objects.equals(score, that.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailAddress, firstName, lastName, institution, count, score);
    }
}
