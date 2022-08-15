package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao;

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
