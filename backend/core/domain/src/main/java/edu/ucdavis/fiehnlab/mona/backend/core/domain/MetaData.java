package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "metadata")
@Profile({"mona.persistence"})
public class MetaData implements Serializable {

    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metadata_count")
    @SequenceGenerator(name = "metadata_count", initialValue = 1, allocationSize = 500)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Spectrum spectrumMetadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Spectrum spectrumAnnotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Compound compoundMetadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Compound compoundClassification;

    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "url")
    private String url;
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "name")
    private String name;
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "value")
    private String value;
    private Boolean hidden;
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "category")
    private String category;
    private Boolean computed;
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "unit")
    private String unit;

    public MetaData() {
    }

    public MetaData(String url, String name, String value, Boolean hidden, String category, Boolean computed) {
        this.url = url;
        this.name = name;
        this.value = value;
        this.hidden = hidden;
        this.category = category;
        this.computed = computed;
        this.unit = "";
    }

    public MetaData(String url, String name, String value, Boolean hidden, String category, Boolean computed, String unit) {
        this.url = url;
        this.name = name;
        this.value = value;
        this.hidden = hidden;
        this.category = category;
        this.computed = computed;
        this.unit = unit;
    }

    public MetaData(String name, String value, Boolean hidden, String category, Boolean computed) {
        this.url = "";
        this.name = name;
        this.value = value;
        this.hidden = hidden;
        this.category = category;
        this.computed = computed;
        this.unit = "";
    }

    public MetaData(MetaData meta) {
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Spectrum getSpectrumMetadata() {
        return spectrumMetadata;
    }

    public void setSpectrumMetadata(Spectrum spectrumMetadata) {
        this.spectrumMetadata = spectrumMetadata;
    }

    public Spectrum getSpectrumAnnotation() {
        return spectrumAnnotation;
    }

    public void setSpectrumAnnotation(Spectrum spectrumAnnotation) {
        this.spectrumAnnotation = spectrumAnnotation;
    }

    public Compound getCompoundMetadata() {
        return compoundMetadata;
    }

    public void setCompoundMetadata(Compound compoundMetadata) {
        this.compoundMetadata = compoundMetadata;
    }

    public Compound getCompoundClassification() {
        return compoundClassification;
    }

    public void setCompoundClassification(Compound compoundClassification) {
        this.compoundClassification = compoundClassification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaData metaData = (MetaData) o;
        return Objects.equals(id, metaData.id) && Objects.equals(spectrumMetadata, metaData.spectrumMetadata) && Objects.equals(spectrumAnnotation, metaData.spectrumAnnotation) && Objects.equals(compoundMetadata, metaData.compoundMetadata) && Objects.equals(compoundClassification, metaData.compoundClassification) && Objects.equals(url, metaData.url) && Objects.equals(name, metaData.name) && Objects.equals(value, metaData.value) && Objects.equals(hidden, metaData.hidden) && Objects.equals(category, metaData.category) && Objects.equals(computed, metaData.computed) && Objects.equals(unit, metaData.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spectrumMetadata, spectrumAnnotation, compoundMetadata, compoundClassification, url, name, value, hidden, category, computed, unit);
    }
}
