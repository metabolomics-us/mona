package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views;

import java.io.Serializable;
import java.util.Objects;

public class TagsId implements Serializable {
    private String monaId;
    private String text;

    public TagsId() {
    }

    public TagsId(String monaId, String text) {
        this.monaId = monaId;
        this.text = text;
    }

    public String getMonaId() {
        return monaId;
    }

    public void setMonaId(String monaId) {
        this.monaId = monaId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagsId tagsId = (TagsId) o;
        return Objects.equals(monaId, tagsId.monaId) && Objects.equals(text, tagsId.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monaId, text);
    }
}
