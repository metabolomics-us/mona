package edu.ucdavis.fiehnlab.mona.backend.core.domain.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Score implements Serializable {
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

    public Double getScore() {
        return score;
    }

    public Double getRelativeScore() {
        return relativeScore;
    }

    public Double getScaledScore() {
        return scaledScore;
    }

    public void setImpacts(List<Impacts> impacts) {
        this.impacts = impacts;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public void setRelativeScore(Double relativeScore) {
        this.relativeScore = relativeScore;
    }

    public void setScaledScore(Double scaledScore) {
        this.scaledScore = scaledScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score1 = (Score) o;
        return Objects.equals(impacts, score1.impacts) && Objects.equals(score, score1.score) && Objects.equals(relativeScore, score1.relativeScore) && Objects.equals(scaledScore, score1.scaledScore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(impacts, score, relativeScore, scaledScore);
    }
}
