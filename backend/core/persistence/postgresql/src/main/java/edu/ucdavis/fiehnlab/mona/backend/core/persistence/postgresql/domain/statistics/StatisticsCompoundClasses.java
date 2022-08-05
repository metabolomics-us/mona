package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics;

import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(StatisticsCompoundClassesId.class)
@Table(name = "statistics_compoundclass")
public class StatisticsCompoundClasses {
    @Id
    private String name;

    private Integer spectrumCount;

    private Integer compoundCount;

    public StatisticsCompoundClasses() {}

    public StatisticsCompoundClasses(String name, Integer spectrumCount, Integer compoundCount) {
        this.name = name;
        this.spectrumCount = spectrumCount;
        this.compoundCount = compoundCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSpectrumCount() {
        return spectrumCount;
    }

    public void setSpectrumCount(Integer spectrumCount) {
        this.spectrumCount = spectrumCount;
    }

    public Integer getCompoundCount() {
        return compoundCount;
    }

    public void setCompoundCount(Integer compoundCount) {
        this.compoundCount = compoundCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticsCompoundClasses that = (StatisticsCompoundClasses) o;
        return Objects.equals(name, that.name) && Objects.equals(spectrumCount, that.spectrumCount) && Objects.equals(compoundCount, that.compoundCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, spectrumCount, compoundCount);
    }
}
