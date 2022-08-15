package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import java.io.Serializable;
import java.util.Objects;

public class SpectrumFeedbackId implements Serializable {
    private Long id;
    private String monaId;
    private String emailAddress;

    public SpectrumFeedbackId(Long id, String monaId, String emailAddress) {
        this.id = id;
        this.monaId = monaId;
        this.emailAddress = emailAddress;
    }

    public SpectrumFeedbackId() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMonaId() {
        return monaId;
    }

    public void setMonaId(String monaId) {
        this.monaId = monaId;
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
        SpectrumFeedbackId that = (SpectrumFeedbackId) o;
        return Objects.equals(id, that.id) && Objects.equals(monaId, that.monaId) && Objects.equals(emailAddress, that.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, monaId, emailAddress);
    }
}
