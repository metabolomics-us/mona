package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao;

import java.io.Serializable;
import java.util.Objects;

public class LibraryDAO implements Serializable {
    private String description;

    private String link;

    private String library;

    private TagDAO tag;

    public LibraryDAO() {
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

    public TagDAO getTag() { return tag; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LibraryDAO that = (LibraryDAO) o;
        return Objects.equals(description, that.description) && Objects.equals(link, that.link) && Objects.equals(library, that.library) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, link, library, tag);
    }
}
