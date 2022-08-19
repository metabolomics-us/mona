package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import java.io.Serializable;
import java.util.Objects;

public class SpectrumResultId implements Serializable {
    private String monaId;

    public SpectrumResultId(String monaId) {
        this.monaId = monaId;
    }

    public SpectrumResultId(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpectrumResultId that = (SpectrumResultId) o;
        return Objects.equals(monaId, that.monaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monaId);
    }

    public String getMonaId() {
        return monaId;
    }

    public void setMonaId(String monaId) {
        this.monaId = monaId;
    }
}
