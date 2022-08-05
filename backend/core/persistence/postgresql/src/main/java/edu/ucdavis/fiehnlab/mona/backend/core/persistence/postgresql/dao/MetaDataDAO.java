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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getComputed() {
        return computed;
    }

    public void setComputed(Boolean computed) {
        this.computed = computed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaDataDAO metaData = (MetaDataDAO) o;
        return Objects.equals(url, metaData.url) && Objects.equals(name, metaData.name) && Objects.equals(value, metaData.value) && Objects.equals(hidden, metaData.hidden) && Objects.equals(category, metaData.category) && Objects.equals(computed, metaData.computed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name, value, hidden, category, computed);
    }
}
