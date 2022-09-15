package edu.ucdavis.fiehnlab.mona.backend.core.domain.views;

import com.vladmihalcea.hibernate.type.json.JsonType;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.MetaDataDAO;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Names;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;
import java.util.Objects;

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
    private List<Tags> tags;

    @Column(name = "inchi")
    private String inchi;

    @Type(type="json")
    @Column(name = "names", columnDefinition = "jsonb")
    private List<Names> names;

    @Column(name = "mol_file")
    private String molFile;

    @Column(name = "computed")
    private Boolean computed;

    @Column(name = "inchikey")
    private String inchikey;

    @Type(type = "json")
    @Column(name = "metadata", columnDefinition = "jsonb")
    private List<MetaDataDAO> metadata;

    @Type(type = "json")
    @Column(name = "classification", columnDefinition = "jsonb")
    private List<MetaDataDAO> classification;

    public Compound() {
    }

    public String getMonaId() {
        return monaId;
    }

    public String getKind() {
        return kind;
    }

    public List<Tags> getTags() {
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

    public String getInchikey() {
        return inchikey;
    }

    public List<MetaDataDAO> getMetadata() {
        return metadata;
    }

    public List<MetaDataDAO> getClassification() {
        return classification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compound compound = (Compound) o;
        return Objects.equals(monaId, compound.monaId) && Objects.equals(kind, compound.kind) && Objects.equals(tags, compound.tags) && Objects.equals(inchi, compound.inchi) && Objects.equals(names, compound.names) && Objects.equals(molFile, compound.molFile) && Objects.equals(computed, compound.computed) && Objects.equals(inchikey, compound.inchikey) && Objects.equals(metadata, compound.metadata) && Objects.equals(classification, compound.classification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monaId, kind, tags, inchi, names, molFile, computed, inchikey, metadata, classification);
    }
}
