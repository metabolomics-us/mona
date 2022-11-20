package edu.ucdavis.fiehnlab.mona.backend.core.domain.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "impacts")
@Profile({"mona.persistence"})
public class Impacts implements Serializable {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "impacts_id")
    @SequenceGenerator(name = "impacts_id", initialValue = 1, allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Score score;
    private Double value;
    private String reason;

    public Impacts() {
    }

    public Impacts(Double value, String reason) {
        this.value = value;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getValue() { return value; }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Impacts impacts = (Impacts) o;
        return id.equals(impacts.id) && score.equals(impacts.score) && Objects.equals(value, impacts.value) && Objects.equals(reason, impacts.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, score, value, reason);
    }
}
