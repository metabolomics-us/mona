package edu.ucdavis.fiehnlab.mona.backend.core.domain.views;

import java.io.Serializable;
import java.util.Objects;

public class SpectraSubmittersId implements Serializable {
    private String monaId;

    private String emailAddress;

    public SpectraSubmittersId() {
    }

    public SpectraSubmittersId(String monaId, String emailAddress) {
        this.monaId = monaId;
        this.emailAddress = emailAddress;
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
        SpectraSubmittersId that = (SpectraSubmittersId) o;
        return Objects.equals(monaId, that.monaId) && Objects.equals(emailAddress, that.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monaId, emailAddress);
    }
}
