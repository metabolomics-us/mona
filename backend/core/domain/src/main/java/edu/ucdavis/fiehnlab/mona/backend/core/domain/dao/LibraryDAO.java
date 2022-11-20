package edu.ucdavis.fiehnlab.mona.backend.core.domain.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "library")
@Profile({"mona.persistence"})
public class LibraryDAO implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "library_id")
    @SequenceGenerator(name = "library_id", initialValue = 1, allocationSize = 50)
    @JsonIgnore
    private Long id;

    @OneToOne(mappedBy = "library")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Spectrum spectrum;

    private String description;

    private String link;

    private String library;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tag_id")
    private TagDAO tag;

    public LibraryDAO() {
    }

    public LibraryDAO(String description, String link, String library, TagDAO tag) {
        this.description = description;
        this.link = link;
        this.library = library;
        this.tag = tag;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Spectrum getSpectrum() {
        return spectrum;
    }

    public void setSpectrum(Spectrum spectrum) {
        this.spectrum = spectrum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public TagDAO getTag() {
        return tag;
    }

    public void setTag(TagDAO tag) {
        this.tag = tag;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryDAO that = (LibraryDAO) o;
        return Objects.equals(id, that.id) && Objects.equals(spectrum, that.spectrum) && Objects.equals(description, that.description) && Objects.equals(link, that.link) && Objects.equals(library, that.library) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spectrum, description, link, library, tag);
    }
}
