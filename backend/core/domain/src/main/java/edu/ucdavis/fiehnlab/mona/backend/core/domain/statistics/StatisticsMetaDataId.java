package edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics;

import java.io.Serializable;
import java.util.Objects;

public class StatisticsMetaDataId implements Serializable {
    private Long id;
    private String name;

    public StatisticsMetaDataId() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        StatisticsMetaDataId that = (StatisticsMetaDataId) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
