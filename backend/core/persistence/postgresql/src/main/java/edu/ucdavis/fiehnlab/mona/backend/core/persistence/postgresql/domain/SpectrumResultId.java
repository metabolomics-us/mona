package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import java.io.Serializable;
import java.util.Objects;

public class SpectrumResultId implements Serializable {
    private Long id;
    private String monaId;

    public SpectrumResultId(Long id, String monaId) {
        this.id = id;
        this.monaId = monaId;
    }

    public SpectrumResultId(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpectrumResultId that = (SpectrumResultId) o;
        return id.equals(that.id) && monaId.equals(that.monaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, monaId);
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
}
