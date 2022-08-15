package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao;

import java.io.Serializable;
import java.util.Objects;

public class MetaDataDAO implements Serializable {
    private String url;
    private String name;
    private String value;
    private Boolean hidden;
    private String category;
    private Boolean computed;
    private String unit;

    public MetaDataDAO() {
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public String getCategory() {
        return category;
    }

    public Boolean getComputed() {
        return computed;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaDataDAO that = (MetaDataDAO) o;
        return Objects.equals(url, that.url) && Objects.equals(name, that.name) && Objects.equals(value, that.value) && Objects.equals(hidden, that.hidden) && Objects.equals(category, that.category) && Objects.equals(computed, that.computed) && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name, value, hidden, category, computed, unit);
    }
}
