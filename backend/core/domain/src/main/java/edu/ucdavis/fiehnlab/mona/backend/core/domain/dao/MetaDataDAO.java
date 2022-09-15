package edu.ucdavis.fiehnlab.mona.backend.core.domain.dao;

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

    public MetaDataDAO(String url, String name, String value, Boolean hidden, String category, Boolean computed, String unit) {
        this.url = url;
        this.name = name;
        this.value = value;
        this.hidden = hidden;
        this.category = category;
        this.computed = computed;
        this.unit = unit;
    }

    public MetaDataDAO(MetaDataDAO meta) {
        this.url = meta.getUrl();
        this.name = meta.getName();
        this.value = meta.getValue();
        this.hidden = meta.getHidden();
        this.category = meta.getCategory();
        this.computed = meta.getComputed();
        this.unit = meta.getUnit();
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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setComputed(Boolean computed) {
        this.computed = computed;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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
