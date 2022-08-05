package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.TypeDef;
import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

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
}
