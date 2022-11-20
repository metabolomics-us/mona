package edu.ucdavis.fiehnlab.mona.backend.core.domain.dao;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumSubmitter;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Sequence.SpectrumSequenceIdGenerator;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.validators.NullOrNotBlank;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "spectrum")
@Profile({"mona.persistence"})
public class Spectrum implements Serializable {
    @Column(name = "compound")
    @NotEmpty
    @NotNull
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "spectrum_id")
    private List<CompoundDAO> compound;

    @Column(name = "id")
    @Size(min = 1)
    @NullOrNotBlank
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spectrum_seq")
    @GenericGenerator(
            name = "spectrum_seq",
            strategy = "edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Sequence.SpectrumSequenceIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = SpectrumSequenceIdGenerator.INCREMENT_PARAM, value = "250"),
                    @org.hibernate.annotations.Parameter(name = SpectrumSequenceIdGenerator.VALUE_PREFIX_PARAMETER, value = "MoNA_"),
                    @org.hibernate.annotations.Parameter(name = SpectrumSequenceIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%07d")
            }
    )
    private String id;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "spectrum_metadata_id")
    @Column(name = "metaData")
    @BatchSize(size = 50)
    private List<MetaDataDAO> metaData;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "spectrum_annotation_id")
    @Column(name = "annotations")
    @BatchSize(size = 50)
    private List<MetaDataDAO> annotations = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "score_id")
    private Score score = new Score(Arrays.asList(new Impacts(0.0,"")), 0.0,0.0,0.0);

    @Column(name = "spectrum")
    @NotEmpty
    @NotBlank
    @Type(type = "org.hibernate.type.TextType")
    private String spectrum;

    private String lastUpdated = new Date().toString();

    private String dateCreated = new Date().toString();

    private String lastCurated = "";

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "splash_id")
    private Splash splash;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "submitter_id")
    @NotNull
    private SpectrumSubmitter submitter;

    @Column(name = "tags")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "spectrum_id")
    private List<TagDAO> tags;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "library_id")
    private LibraryDAO library;

    public Spectrum() {
    }

    public Spectrum(List<CompoundDAO> compound, String id, List<MetaDataDAO> metaData, List<MetaDataDAO> annotations, Score score, String spectrum, String lastUpdated, String dateCreated, String lastCurated, Splash splash, SpectrumSubmitter submitter, List<TagDAO> tags, LibraryDAO library) {
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

    public SpectrumSubmitter getSubmitter() {
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

    public void setSubmitter(SpectrumSubmitter submitter) {
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
