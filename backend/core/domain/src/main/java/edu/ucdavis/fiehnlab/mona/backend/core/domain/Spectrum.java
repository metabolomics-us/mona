package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Sequence.SpectrumSequenceIdGenerator;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.validators.NullOrNotBlank;
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
    private List<Compound> compound;

    @Column(name = "id")
    @Size(min = 1)
    @NullOrNotBlank
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spectrum_seq")
    @GenericGenerator(
            name = "spectrum_seq",
            strategy = "edu.ucdavis.fiehnlab.mona.backend.core.domain.Sequence.SpectrumSequenceIdGenerator",
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
    private List<MetaData> metaData;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "spectrum_annotation_id")
    @Column(name = "annotations")
    private List<MetaData> annotations = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "score_id")
    private Score score;

    @Column(name = "spectrum")
    @NotEmpty
    @NotBlank
    @Type(type = "org.hibernate.type.TextType")
    private String spectrum;

    @Temporal(TemporalType.DATE)
    private Date lastUpdated = new Date();

    @Temporal(TemporalType.DATE)
    private Date dateCreated = new Date();

    @Temporal(TemporalType.DATE)
    private Date lastCurated = null;

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
    private List<Tag> tags;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "library_id")
    private Library library;

    public Spectrum() {
    }

    public Spectrum(List<Compound> compound, String id, List<MetaData> metaData, List<MetaData> annotations, Score score, String spectrum, Date lastUpdated, Date dateCreated, Date lastCurated, Splash splash, SpectrumSubmitter submitter, List<Tag> tags, Library library) {
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


    public List<Compound> getCompound() {
        return compound;
    }

    public String getId() {
        return id;
    }

    public List<MetaData> getMetaData() {
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

    public List<Tag> getTags() {
        return tags;
    }

    public Library getLibrary() {
        return library;
    }

    public Date getLastUpdated() { return lastUpdated; }

    public Date getDateCreated() { return dateCreated; }

    public Date getLastCurated() { return lastCurated; }

    public List<MetaData> getAnnotations() { return annotations; }

    public void setCompound(List<Compound> compound) {
        this.compound = compound;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMetaData(List<MetaData> metaData) {
        this.metaData = metaData;
    }

    public void setAnnotations(List<MetaData> annotations) {
        this.annotations = annotations;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public void setSpectrum(String spectrum) {
        this.spectrum = spectrum;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setLastCurated(Date lastCurated) {
        this.lastCurated = lastCurated;
    }

    public void setSplash(Splash splash) {
        this.splash = splash;
    }

    public void setSubmitter(SpectrumSubmitter submitter) {
        this.submitter = submitter;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void setLibrary(Library library) {
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
