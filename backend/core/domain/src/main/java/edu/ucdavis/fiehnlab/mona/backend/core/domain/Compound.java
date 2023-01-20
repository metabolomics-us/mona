package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "compound")
@Profile({"mona.persistence"})
public class Compound implements Serializable {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "compound_count")
    @SequenceGenerator(name = "compound_count", initialValue = 1, allocationSize = 200)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Spectrum spectrumCompound;

    @Column(name = "kind")
    private String kind;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "compound_tags_id")
    private List<Tag> tags;

    @Column(name = "inchi")
    @Type(type = "org.hibernate.type.TextType")
    private String inchi;

    @Column(name = "names")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "compound_names_id")
    private List<Names> names;

    @Column(name = "molFile")
    @Type(type = "org.hibernate.type.TextType")
    private String molFile;

    @Column(name = "computed")
    private Boolean computed;

    @Column(name = "inchiKey")
    @Type(type = "org.hibernate.type.TextType")
    private String inchiKey;

    @Column(name = "metaData")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "compound_metadata_id")
    private List<MetaData> metaData;

    @Column(name = "classification")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "compound_classification_id")
    private List<MetaData> classification = new ArrayList<>();

    public Compound() {
    }

    public Compound(String kind, List<Tag> tags, String inchi, List<Names> names, String molFile, Boolean computed, String inchiKey, List<MetaData> metaData, List<MetaData> classification) {
        this.kind = kind;
        this.tags = tags;
        this.inchi = inchi;
        this.names = names;
        this.molFile = molFile;
        this.computed = computed;
        this.inchiKey = inchiKey;
        this.metaData = metaData;
        this.classification = classification;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setInchi(String inchi) {
        this.inchi = inchi;
    }

    public Spectrum getSpectrumCompound() {
        return spectrumCompound;
    }

    public void setSpectrumCompound(Spectrum spectrumCompound) {
        this.spectrumCompound = spectrumCompound;
    }

    public void setNames(List<Names> names) {
        this.names = names;
    }

    public void setMolFile(String molFile) {
        this.molFile = molFile;
    }

    public void setComputed(Boolean computed) {
        this.computed = computed;
    }

    public void setInchiKey(String inchiKey) {
        this.inchiKey = inchiKey;
    }

    public void setMetaData(List<MetaData> metaData) {
        this.metaData = metaData;
    }

    public void setClassification(List<MetaData> classification) {
        this.classification = classification;
    }

    public String getKind() {
        return kind;
    }

    public List<Tag> getTags() {
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

    public List<MetaData> getMetaData() {
        return metaData;
    }

    public List<MetaData> getClassification() {
        return classification;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compound compound = (Compound) o;
        return Objects.equals(id, compound.id) && Objects.equals(spectrumCompound, compound.spectrumCompound) && Objects.equals(kind, compound.kind) && Objects.equals(tags, compound.tags) && Objects.equals(inchi, compound.inchi) && Objects.equals(names, compound.names) && Objects.equals(molFile, compound.molFile) && Objects.equals(computed, compound.computed) && Objects.equals(inchiKey, compound.inchiKey) && Objects.equals(metaData, compound.metaData) && Objects.equals(classification, compound.classification);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spectrumCompound, kind, tags, inchi, names, molFile, computed, inchiKey, metaData, classification);
    }
}
