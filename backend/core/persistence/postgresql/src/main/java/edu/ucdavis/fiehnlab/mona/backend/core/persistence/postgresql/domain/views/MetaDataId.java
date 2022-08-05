package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views;

import java.io.Serializable;
import java.util.Objects;

public class MetaDataId implements Serializable{
    private String monaId;
    private String name;
    private String value;

    public MetaDataId() {}

    public MetaDataId(String monaId, String name, String value) {
        this.monaId = monaId;
        this.name = name;
        this.value = value;
    }

    public String getMonaId() {
        return monaId;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setMonaId(String monaId) {
        this.monaId = monaId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaDataId that = (MetaDataId) o;
        return Objects.equals(monaId, that.monaId) && Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monaId, name, value);
    }
}
