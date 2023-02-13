package edu.ucdavis.fiehnlab.mona.backend.core.domain;

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
public class Library implements Serializable {
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

    public Library() {
    }

    public Library(String description, String link, String library) {
        this.description = description;
        this.link = link;
        this.library = library;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Library library1 = (Library) o;
        return Objects.equals(id, library1.id) && Objects.equals(spectrum, library1.spectrum) && Objects.equals(description, library1.description) && Objects.equals(link, library1.link) && Objects.equals(library, library1.library);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spectrum, description, link, library);
    }
}
