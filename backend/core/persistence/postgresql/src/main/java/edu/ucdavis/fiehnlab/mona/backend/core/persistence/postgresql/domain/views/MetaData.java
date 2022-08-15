package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.TypeDef;
import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.util.Objects;

@Entity
@TypeDef(
        name = "json",
        typeClass = JsonType.class
)
@IdClass(MetaDataId.class)
@Subselect("select * from metadata")
@Immutable
public class MetaData {
    @Id
    private String monaId;

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "unit")
    private String unit;

    @Id
    @Column(name = "value")
    private String value;

    @Column(name = "hidden")
    private Boolean hidden;

    @Column(name = "category")
    private String category;

    @Column(name = "computed")
    private Boolean computed;

    public String getMonaId() {
        return monaId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaData metaData = (MetaData) o;
        return Objects.equals(monaId, metaData.monaId) && Objects.equals(name, metaData.name) && Objects.equals(unit, metaData.unit) && Objects.equals(value, metaData.value) && Objects.equals(hidden, metaData.hidden) && Objects.equals(category, metaData.category) && Objects.equals(computed, metaData.computed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monaId, name, unit, value, hidden, category, computed);
    }
}
