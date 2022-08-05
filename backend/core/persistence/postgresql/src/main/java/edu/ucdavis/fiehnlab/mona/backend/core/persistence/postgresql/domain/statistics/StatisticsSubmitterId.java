package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics;


import java.io.Serializable;
import java.util.Objects;

public class StatisticsSubmitterId implements Serializable {
    private String emailAddress;

    public StatisticsSubmitterId() {
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticsSubmitterId that = (StatisticsSubmitterId) o;
        return Objects.equals(emailAddress, that.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailAddress);
    }
}
