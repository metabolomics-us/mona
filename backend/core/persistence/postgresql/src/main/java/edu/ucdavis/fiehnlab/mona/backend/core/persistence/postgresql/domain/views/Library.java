package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.util.Objects;

@Entity
@IdClass(LibraryId.class)
@Subselect("select * from library")
@Immutable
public class Library {
    @Id
    private String monaId;

    private String description;

    private String link;

    @Id
    private String library;

    private String text;

    private Boolean ruleBased;

    public Library() {
    }

    public Library(String monaId, String description, String link, String library, String text, Boolean ruleBased) {
        this.monaId = monaId;
        this.description = description;
        this.link = link;
        this.library = library;
        this.text = text;
        this.ruleBased = ruleBased;
    }

    public String getMonaId() {
        return monaId;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public String getLibrary() {
        return library;
    }

    public String getText() {
        return text;
    }

    public Boolean getRuleBased() {
        return ruleBased;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Library library1 = (Library) o;
        return Objects.equals(monaId, library1.monaId) && Objects.equals(description, library1.description) && Objects.equals(link, library1.link) && Objects.equals(library, library1.library) && Objects.equals(text, library1.text) && Objects.equals(ruleBased, library1.ruleBased);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monaId, description, link, library, text, ruleBased);
    }
}
