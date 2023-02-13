package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(SpectrumFeedbackId.class)
@Table(name = "spectrum_feedback")
@Profile({"mona.persistence"})
public class SpectrumFeedback implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spectrum_feedback_id")
    @SequenceGenerator(name = "spectrum_feedback_id", initialValue = 1, allocationSize = 50)
    private Long id;

    //should be a foreign key
    @Id
    private String monaId;

    //can be our only primary key
    @Id
    private String emailAddress;

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

    public void setMonaId(String monaId) {
        this.monaId = monaId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
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

    public SpectrumFeedback(String monaId, String emailAddress, String name, String value) {
        this.monaId = monaId;
        this.emailAddress = emailAddress;
        this.name = name;
        this.value = value;
    }
}
