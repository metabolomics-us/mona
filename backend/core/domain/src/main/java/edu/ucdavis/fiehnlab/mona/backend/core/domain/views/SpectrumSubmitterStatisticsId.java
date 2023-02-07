package edu.ucdavis.fiehnlab.mona.backend.core.domain.views;

import java.io.Serializable;
import java.util.Objects;

public class SpectrumSubmitterStatisticsId implements Serializable {
    private String id;

    private String emailAddress;

    public SpectrumSubmitterStatisticsId() {
    }

    public SpectrumSubmitterStatisticsId(String id, String emailAddress) {
        this.id = id;
        this.emailAddress = emailAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        SpectrumSubmitterStatisticsId that = (SpectrumSubmitterStatisticsId) o;
        return Objects.equals(id, that.id) && Objects.equals(emailAddress, that.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, emailAddress);
    }
}
