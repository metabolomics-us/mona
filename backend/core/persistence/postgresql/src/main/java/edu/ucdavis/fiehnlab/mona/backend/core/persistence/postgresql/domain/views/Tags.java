package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(TagsId.class)
@Subselect("select * from tags")
@Immutable
public class Tags {
    @Id
    private String monaId;

    @Id
    private String text;

    private Boolean ruleBased;

    public Tags() {
    }

    public String getMonaId() {
        return monaId;
    }

    public String getText() {
        return text;
    }

    public Boolean getRuleBased() {
        return ruleBased;
    }
}
