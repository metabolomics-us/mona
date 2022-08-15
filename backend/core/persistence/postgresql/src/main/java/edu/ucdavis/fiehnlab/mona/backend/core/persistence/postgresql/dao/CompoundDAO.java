package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@TypeDef(
        name = "json",
        typeClass = JsonType.class
)
public class CompoundDAO implements Serializable {
    @Column(name = "kind")
    private String kind;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private List<TagDAO> tags;

    @Column(name = "inchi")
    private String inchi;

    @Type(type = "json")
    @Column(name = "names", columnDefinition = "jsonb")
    private List<Names> names;

    @Column(name = "molFile")
    private String molFile;

    @Column(name = "computed")
    private Boolean computed;

    @Column(name = "inchiKey")
    private String inchiKey;

    @Type(type = "json")
    @Column(name = "metaData", columnDefinition = "jsonb")
    private List<MetaDataDAO> metaData;

    @Type(type = "json")
    @Column(name = "classification", columnDefinition = "jsonb")
    private List<MetaDataDAO> classification = Collections.emptyList();

    public CompoundDAO() {
    }

    public String getKind() {
        return kind;
    }

    public List<TagDAO> getTags() {
        return tags;
    }

    public String getInchi() {
        return inchi;
    }

    public List<Names> getNames() {
        return names;
    }

    public String getMolFile() {
        return molFile;
    }

    public Boolean getComputed() {
        return computed;
    }

    public String getInchiKey() {
        return inchiKey;
    }

    public List<MetaDataDAO> getMetaData() {
        return metaData;
    }

    public List<MetaDataDAO> getClassification() {
        return classification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompoundDAO that = (CompoundDAO) o;
        return Objects.equals(kind, that.kind) && Objects.equals(tags, that.tags) && Objects.equals(inchi, that.inchi) && Objects.equals(names, that.names) && Objects.equals(molFile, that.molFile) && Objects.equals(computed, that.computed) && Objects.equals(inchiKey, that.inchiKey) && Objects.equals(metaData, that.metaData) && Objects.equals(classification, that.classification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, tags, inchi, names, molFile, computed, inchiKey, metaData, classification);
    }
}
