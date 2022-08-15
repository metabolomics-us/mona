package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Objects;

public class TagDAO implements Serializable {
    private String text;
    @Column(name = "ruleBased")
    private Boolean ruleBased;

    public TagDAO() {
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
        TagDAO tagDAO = (TagDAO) o;
        return Objects.equals(text, tagDAO.text) && Objects.equals(ruleBased, tagDAO.ruleBased);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, ruleBased);
    }
}
