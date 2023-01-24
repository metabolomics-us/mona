package edu.ucdavis.fiehnlab.mona.backend.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "score")
@Profile({"mona.persistence"})
public class Score implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "score_id")
    @SequenceGenerator(name = "score_id", initialValue = 1, allocationSize = 50)
    @JsonIgnore
    private Long id;

    @OneToOne(mappedBy = "score")
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Spectrum spectrum;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "score_id")
    private List<Impacts> impacts;
    private Double score;
    private Double relativeScore;
    private Double scaledScore;

    public Score() {
    }

    public Score(List<Impacts> impacts, Double score, Double relativeScore, Double scaledScore) {
        this.impacts = impacts;
        this.score = score;
        this.relativeScore = relativeScore;
        this.scaledScore = scaledScore;
    }

    public List<Impacts> getImpacts() {
        return impacts;
    }

    public void setImpacts(List<Impacts> impacts) {
        this.impacts = impacts;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Double getRelativeScore() {
        return relativeScore;
    }

    public void setRelativeScore(Double relativeScore) {
        this.relativeScore = relativeScore;
    }

    public Double getScaledScore() {
        return scaledScore;
    }

    public void setScaledScore(Double scaledScore) {
        this.scaledScore = scaledScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score1 = (Score) o;
        return id.equals(score1.id) && spectrum.equals(score1.spectrum) && Objects.equals(impacts, score1.impacts) && Objects.equals(score, score1.score) && Objects.equals(relativeScore, score1.relativeScore) && Objects.equals(scaledScore, score1.scaledScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, spectrum, impacts, score, relativeScore, scaledScore);
    }
}
