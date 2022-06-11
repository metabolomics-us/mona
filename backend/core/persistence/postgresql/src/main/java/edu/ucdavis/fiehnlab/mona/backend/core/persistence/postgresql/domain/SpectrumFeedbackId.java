package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class SpectrumFeedbackId implements Serializable {
    private Long id;
    private String monaId;
    private String userId;

    public SpectrumFeedbackId(Long id, String monaId, String userId) {
        this.id = id;
        this.monaId = monaId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpectrumFeedbackId that = (SpectrumFeedbackId) o;
        return id.equals(that.id) && monaId.equals(that.monaId) && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, monaId, userId);
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
