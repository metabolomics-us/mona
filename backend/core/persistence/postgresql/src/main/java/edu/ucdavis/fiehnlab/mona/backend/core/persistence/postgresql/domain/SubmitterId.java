package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import java.io.Serializable;
import java.util.Objects;

public class SubmitterId implements Serializable {
    private Long id;
    private String emailAddress;

    public SubmitterId(Long id, String emailAddress) {
        this.id = id;
        this.emailAddress = emailAddress;
    }

    public SubmitterId(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubmitterId that = (SubmitterId) o;
        return id.equals(that.id) && emailAddress.equals(that.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, emailAddress);
    }

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
}
