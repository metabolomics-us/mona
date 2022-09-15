package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import java.io.Serializable;
import java.util.Objects;

public class SubmitterId implements Serializable {
    private String emailAddress;

    public SubmitterId(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public SubmitterId(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubmitterId that = (SubmitterId) o;
        return Objects.equals(emailAddress, that.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailAddress);
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
