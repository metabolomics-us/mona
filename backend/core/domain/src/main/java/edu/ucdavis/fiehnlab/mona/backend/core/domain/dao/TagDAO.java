package edu.ucdavis.fiehnlab.mona.backend.core.domain.dao;

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
public class TagDAO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tag_id")
    @SequenceGenerator(name = "tag_id", initialValue = 1, allocationSize = 50)
    @JsonIgnore
    private Long id;

    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Spectrum spectrum;

    @ManyToOne()
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private CompoundDAO compound;

    @OneToOne()
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private LibraryDAO library;

    private String text;

    private Boolean ruleBased;

    public TagDAO() {
    }

    public TagDAO(String text, Boolean ruleBased) {
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

    public CompoundDAO getCompound() { return compound;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagDAO tagDAO = (TagDAO) o;
        return id.equals(tagDAO.id) && spectrum.equals(tagDAO.spectrum) && compound.equals(tagDAO.compound) && library.equals(tagDAO.library) && Objects.equals(text, tagDAO.text) && Objects.equals(ruleBased, tagDAO.ruleBased);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spectrum, compound, library, text, ruleBased);
    }
}
