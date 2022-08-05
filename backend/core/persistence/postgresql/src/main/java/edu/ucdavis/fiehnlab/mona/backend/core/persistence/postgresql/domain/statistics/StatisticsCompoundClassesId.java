package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics;

import java.io.Serializable;
import java.util.Objects;

public class StatisticsCompoundClassesId implements Serializable {
    private String name;

    public StatisticsCompoundClassesId() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticsCompoundClassesId that = (StatisticsCompoundClassesId) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
