package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import java.io.Serializable;
import java.util.Objects;

public class UserId implements Serializable {
    private Long id;
    private String emailAddress;

    public UserId(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(id, userId.id) && Objects.equals(emailAddress, userId.emailAddress);
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
