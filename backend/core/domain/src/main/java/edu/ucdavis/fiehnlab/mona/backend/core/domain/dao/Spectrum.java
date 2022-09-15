package edu.ucdavis.fiehnlab.mona.backend.core.domain.dao;

import com.vladmihalcea.hibernate.type.json.JsonType;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.validators.NullOrNotBlank;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@TypeDef(
        name = "json",
        typeClass = JsonType.class
)
public class Spectrum implements Serializable {
    @Type(type = "json")
    @Column(name = "compound", columnDefinition = "jsonb")
    @NotEmpty
    @NotNull
    private List<CompoundDAO> compound;

    @Column(name = "id")
    @Size(min = 1)
    @NullOrNotBlank
    private String id;

    @Type(type = "json")
    @Column(name = "metaData", columnDefinition = "jsonb")
    private List<MetaDataDAO> metaData;

    @Type(type = "json")
    @Column(columnDefinition = "jsonb")
    private List<MetaDataDAO> annotations = Collections.EMPTY_LIST;

    @Type(type = "json")
    @Column( name = "score", columnDefinition = "jsonb")
    private Score score;

    @Column(name = "spectrum")
    @NotEmpty
    @NotBlank
    private String spectrum;

    private String lastUpdated = new Date().toString();

    private String dateCreated = new Date().toString();

    private String lastCurated;

    @Type(type = "json")
    @Column(name = "splash", columnDefinition = "jsonb")
    private Splash splash;

    @Type(type = "json")
    @Column(name = "submitter", columnDefinition = "jsonb")
    @NotNull
    private SubmitterDAO submitter;

    @Type(type = "json")
    @Column(name = "tags", columnDefinition = "jsonb")
    private List<TagDAO> tags;

    @Type(type = "json")
    @Column(name = "library", columnDefinition = "jsonb")
    private LibraryDAO library;

    public Spectrum() {
    }

    public Spectrum(List<CompoundDAO> compound, String id, List<MetaDataDAO> metaData, List<MetaDataDAO> annotations, Score score, String spectrum, String lastUpdated, String dateCreated, String lastCurated, Splash splash, SubmitterDAO submitter, List<TagDAO> tags, LibraryDAO library) {
        this.compound = compound;
        this.id = id;
        this.metaData = metaData;
        this.annotations = annotations;
        this.score = score;
        this.spectrum = spectrum;
        this.lastUpdated = lastUpdated;
        this.dateCreated = dateCreated;
        this.lastCurated = lastCurated;
        this.splash = splash;
        this.submitter = submitter;
        this.tags = tags;
        this.library = library;
    }

    public Spectrum(Spectrum spectrum) {
        this.compound = spectrum.getCompound();
        this.id = spectrum.getId();
        this.metaData = spectrum.getMetaData();
        this.annotations = spectrum.getAnnotations();
        this.score = spectrum.getScore();
        this.spectrum = spectrum.getSpectrum();
        this.lastUpdated = spectrum.getLastUpdated();
        this.dateCreated = spectrum.getDateCreated();
        this.lastCurated = spectrum.getLastCurated();
        this.splash = spectrum.getSplash();
        this.submitter = spectrum.getSubmitter();
        this.tags = spectrum.getTags();
        this.library = spectrum.getLibrary();
    }

    public List<CompoundDAO> getCompound() {
        return compound;
    }

    public String getId() {
        return id;
    }

    public List<MetaDataDAO> getMetaData() {
        return metaData;
    }

    public Score getScore() {
        return score;
    }

    public String getSpectrum() {
        return spectrum;
    }

    public Splash getSplash() {
        return splash;
    }

    public SubmitterDAO getSubmitter() {
        return submitter;
    }

    public List<TagDAO> getTags() {
        return tags;
    }

    public LibraryDAO getLibrary() {
        return library;
    }

    public String getLastUpdated() { return lastUpdated; }

    public String getDateCreated() { return dateCreated; }

    public String getLastCurated() { return lastCurated; }

    public List<MetaDataDAO> getAnnotations() { return annotations; }

    public void setCompound(List<CompoundDAO> compound) {
        this.compound = compound;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMetaData(List<MetaDataDAO> metaData) {
        this.metaData = metaData;
    }

    public void setAnnotations(List<MetaDataDAO> annotations) {
        this.annotations = annotations;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public void setSpectrum(String spectrum) {
        this.spectrum = spectrum;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setLastCurated(String lastCurated) {
        this.lastCurated = lastCurated;
    }

    public void setSplash(Splash splash) {
        this.splash = splash;
    }

    public void setSubmitter(SubmitterDAO submitter) {
        this.submitter = submitter;
    }

    public void setTags(List<TagDAO> tags) {
        this.tags = tags;
    }

    public void setLibrary(LibraryDAO library) {
        this.library = library;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spectrum spectrum1 = (Spectrum) o;
        return Objects.equals(compound, spectrum1.compound) && Objects.equals(id, spectrum1.id) && Objects.equals(metaData, spectrum1.metaData) && Objects.equals(annotations, spectrum1.annotations) && Objects.equals(score, spectrum1.score) && Objects.equals(spectrum, spectrum1.spectrum) && Objects.equals(lastUpdated, spectrum1.lastUpdated) && Objects.equals(dateCreated, spectrum1.dateCreated) && Objects.equals(lastCurated, spectrum1.lastCurated) && Objects.equals(splash, spectrum1.splash) && Objects.equals(submitter, spectrum1.submitter) && Objects.equals(tags, spectrum1.tags) && Objects.equals(library, spectrum1.library);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compound, id, metaData, annotations, score, spectrum, lastUpdated, dateCreated, lastCurated, splash, submitter, tags, library);
    }
}
