package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain;

import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(SpectrumFeedbackId.class)
@Table(name = "spectrum_feedback")
public class SpectrumFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spectrum_feedback_id")
    @SequenceGenerator(name = "spectrum_feedback_id", initialValue = 1, allocationSize = 50)
    private Long id;

    //should be a foreign key
    @Id
    private String monaId;

    //can be our only primary key
    @Id
    private String userId;

    private String name;

    private String value;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMonaId() {
        return monaId;
    }

    public void setMonaId(String mona_id) {
        this.monaId = monaId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SpectrumFeedback() {}

    public SpectrumFeedback(String monaId, String userId, String name, String value) {
        this.monaId = monaId;
        this.userId = userId;
        this.name = name;
        this.value = value;
    }
}
