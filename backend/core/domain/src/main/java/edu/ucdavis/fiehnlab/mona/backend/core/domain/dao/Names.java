package edu.ucdavis.fiehnlab.mona.backend.core.domain.dao;

import java.io.Serializable;
import java.util.Objects;

public class Names implements Serializable {
    private Boolean computed;
    private String name;
    private Double score;
    private String source;

    public Names() {
    }

    public Names(Boolean computed, String name, Double score, String source) {
        this.computed = computed;
        this.name = name;
        this.score = score;
        this.source = source;
    }
    public Boolean getComputed() {
        return computed;
    }

    public String getName() {
        return name;
    }

    public Double getScore() {
        return score;
    }

    public String getSource() { return source;}

    public void setComputed(Boolean computed) {
        this.computed = computed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Names names = (Names) o;
        return Objects.equals(computed, names.computed) && Objects.equals(name, names.name) && Objects.equals(score, names.score) && Objects.equals(source, names.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(computed, name, score, source);
    }
}
