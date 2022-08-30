package edu.ucdavis.fiehnlab.mona.backend.core.domain.dao;

import java.io.Serializable;
import java.util.Objects;

public class Names implements Serializable {
    private Boolean computed;
    private String name;
    private Long score;
    private String source;

    public Names() {
    }

    public Boolean getComputed() {
        return computed;
    }

    public String getName() {
        return name;
    }

    public Long getScore() {
        return score;
    }

    public String getSource() { return source;}

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
