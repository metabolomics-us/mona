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
    private Spectrum spectrum;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Compound compound;

    private String text;

    private Boolean ruleBased;

    public Tag() {
    }

    public Tag(String text, Boolean ruleBased) {
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

    public Spectrum getSpectrum() { return spectrum; }

    public Compound getCompound() { return compound;}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(id, tag.id) && Objects.equals(spectrum, tag.spectrum) && Objects.equals(compound, tag.compound) && Objects.equals(text, tag.text) && Objects.equals(ruleBased, tag.ruleBased);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spectrum, compound, text, ruleBased);
    }
}
