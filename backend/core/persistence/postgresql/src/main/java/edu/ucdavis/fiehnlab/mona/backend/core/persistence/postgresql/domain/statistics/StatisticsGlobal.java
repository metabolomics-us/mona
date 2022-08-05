package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "statistics_global")
public class StatisticsGlobal {
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "statistics_id")
    @SequenceGenerator(name = "statistics_id", initialValue = 1, allocationSize = 50)
    @Id
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", nullable = false)
    private Date created;

    private Long spectrumCount;

    private Long compoundCount;

    private Long metaDataCount;

    private Long metaDataValueCount;

    private Long tagCount;

    private Long tagValueCount;

    private Long submitterCount;

    public StatisticsGlobal() {
    }

    public StatisticsGlobal(Date created, Long spectrumCount, Long compoundCount, Long metaDataCount, Long metaDataValueCount, Long tagCount, Long tagValueCount, Long submitterCount) {
        this.created = created;
        this.spectrumCount = spectrumCount;
        this.compoundCount = compoundCount;
        this.metaDataCount = metaDataCount;
        this.metaDataValueCount = metaDataValueCount;
        this.tagCount = tagCount;
        this.tagValueCount = tagValueCount;
        this.submitterCount = submitterCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Long getSpectrumCount() {
        return spectrumCount;
    }

    public void setSpectrumCount(Long spectrumCount) {
        this.spectrumCount = spectrumCount;
    }

    public Long getCompoundCount() {
        return compoundCount;
    }

    public void setCompoundCount(Long compoundCount) {
        this.compoundCount = compoundCount;
    }

    public Long getMetaDataCount() {
        return metaDataCount;
    }

    public void setMetaDataCount(Long metaDataCount) {
        this.metaDataCount = metaDataCount;
    }

    public Long getMetaDataValueCount() {
        return metaDataValueCount;
    }

    public void setMetaDataValueCount(Long metaDataValueCount) {
        this.metaDataValueCount = metaDataValueCount;
    }

    public Long getTagCount() {
        return tagCount;
    }

    public void setTagCount(Long tagCount) {
        this.tagCount = tagCount;
    }

    public Long getTagValueCount() {
        return tagValueCount;
    }

    public void setTagValueCount(Long tagValueCount) {
        this.tagValueCount = tagValueCount;
    }

    public Long getSubmitterCount() {
        return submitterCount;
    }

    public void setSubmitterCount(Long submitterCount) {
        this.submitterCount = submitterCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticsGlobal that = (StatisticsGlobal) o;
        return Objects.equals(id, that.id) && Objects.equals(created, that.created) && Objects.equals(spectrumCount, that.spectrumCount) && Objects.equals(compoundCount, that.compoundCount) && Objects.equals(metaDataCount, that.metaDataCount) && Objects.equals(metaDataValueCount, that.metaDataValueCount) && Objects.equals(tagCount, that.tagCount) && Objects.equals(tagValueCount, that.tagValueCount) && Objects.equals(submitterCount, that.submitterCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, created, spectrumCount, compoundCount, metaDataCount, metaDataValueCount, tagCount, tagValueCount, submitterCount);
    }
}
