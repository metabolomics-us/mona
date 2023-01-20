package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "tags")
@Profile({"mona.persistence"})
public class Tag implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_id")
    @SequenceGenerator(name = "tag_id", initialValue = 1, allocationSize = 50)
    @JsonIgnore
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Spectrum spectrumTags;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Compound compoundTags;

    @OneToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Library libraryTag;

    private String text;

    private Boolean ruleBased;

    public Tag() {
    }

    public Tag(String text, Boolean ruleBased) {
        this.text = text;
        this.ruleBased = ruleBased;
    }

    public Tag(Long id, Spectrum spectrumTags, Compound compoundTags, Library libraryTag, String text, Boolean ruleBased) {
        this.id = id;
        this.spectrumTags = spectrumTags;
        this.compoundTags = compoundTags;
        this.libraryTag = libraryTag;
        this.text = text;
        this.ruleBased = ruleBased;
    }

    public Tag(Spectrum spectrumTags, Compound compoundTags, Library libraryTag, String text, Boolean ruleBased) {
        this.spectrumTags = spectrumTags;
        this.compoundTags = compoundTags;
        this.libraryTag = libraryTag;
        this.text = text;
        this.ruleBased = ruleBased;
    }

    public Tag(Spectrum spectrumTags, Compound compoundTags, String text, Boolean ruleBased) {
        this.spectrumTags = spectrumTags;
        this.compoundTags = compoundTags;
        this.text = text;
        this.ruleBased = ruleBased;
    }

    public String getText() {
        return text;
    }

    public Boolean getRuleBased() {
        return ruleBased;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setRuleBased(Boolean ruleBased) {
        this.ruleBased = ruleBased;
    }

    public Spectrum getSpectrumTags() { return spectrumTags; }

    public Compound getCompoundTags() { return compoundTags;}

    public Library getLibraryTag() { return libraryTag; }

    public void setSpectrumTags(Spectrum spectrumTags) {
        this.spectrumTags = spectrumTags;
    }

    public void setCompoundTags(Compound compoundTags) {
        this.compoundTags = compoundTags;
    }

    public void setLibraryTag(Library libraryTag) {
        this.libraryTag = libraryTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(id, tag.id) && Objects.equals(spectrumTags, tag.spectrumTags) && Objects.equals(compoundTags, tag.compoundTags) && Objects.equals(libraryTag, tag.libraryTag) && Objects.equals(text, tag.text) && Objects.equals(ruleBased, tag.ruleBased);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spectrumTags, compoundTags, libraryTag, text, ruleBased);
    }
}
