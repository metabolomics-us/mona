package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@TypeDef(
        name = "json",
        typeClass = JsonType.class
)
@Subselect("select * from compound")
@Immutable
public class Compound {
    @Id
    private String monaId;

    @Column(name = "kind")
    private String kind;

    @Type(type="json")
    @Column(columnDefinition = "jsonb", name="tags")
    private String tags;

    @Column(name = "inchi")
    private String inchi;

    @Type(type="json")
    @Column(name = "names", columnDefinition = "jsonb")
    private String names;

    @Column(name = "mol_file")
    private String molFile;

    @Column(name = "computed")
    private Boolean computed;

    @Column(name = "inchikey")
    private String inchikey;

    @Type(type = "json")
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;
    @Type(type = "json")
    @Column(name = "classification", columnDefinition = "jsonb")

    private String classification;

    public String getMonaId() {
        return monaId;
    }

    public String getKind() {
        return kind;
    }

    public String getTags() {
        return tags;
    }

    public String getInchi() {
        return inchi;
    }

    public String getNames() {
        return names;
    }

    public String getMolFile() {
        return molFile;
    }

    public Boolean getComputed() {
        return computed;
    }

    public String getInchikey() {
        return inchikey;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getClassification() {
        return classification;
    }
}
