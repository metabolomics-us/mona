package edu.ucdavis.fiehnlab.mona.backend.core.domain.views;

import java.io.Serializable;
import java.util.Objects;

public class LibraryId implements Serializable {
    private String monaId;

    private String library;

    public LibraryId() {
    }

    public LibraryId(String monaId, String library) {
        this.monaId = monaId;
        this.library = library;
    }

    public String getMonaId() {
        return monaId;
    }

    public void setMonaId(String monaId) {
        this.monaId = monaId;
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
        LibraryId libraryId = (LibraryId) o;
        return Objects.equals(monaId, libraryId.monaId) && Objects.equals(library, libraryId.library);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monaId, library);
    }
}
