package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao;

import java.io.Serializable;
import java.util.Objects;

public class Impacts implements Serializable {
    private Double value;
    private String reason;

    public Impacts() {
    }

   public Double getValue() { return value; }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Impacts impacts = (Impacts) o;
        return Objects.equals(value, impacts.value) && Objects.equals(reason, impacts.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, reason);
    }
}
