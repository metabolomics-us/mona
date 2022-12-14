package edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Type;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "metadata_value_count")
@Profile({"mona.persistence"})
public class MetaDataValueCount implements Serializable {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metadata_value_count_id")
    @SequenceGenerator(name = "metadata_value_count", initialValue = 1, allocationSize = 50)
    private Long id;

    @Type(type = "org.hibernate.type.TextType")
    private String value;

    private Integer count;

    @ManyToOne
    @JsonIgnore
    @JoinColumns({
            @JoinColumn(name="statistics_metadata_id"),
            @JoinColumn(name="statistics_metadata_name")
    })
    private StatisticsMetaData statisticsMetaData;

    public MetaDataValueCount() {
    }

    public MetaDataValueCount(String value, Integer count) {
        this.value = value;
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaDataValueCount that = (MetaDataValueCount) o;
        return Objects.equals(id, that.id) && Objects.equals(value, that.value) && Objects.equals(count, that.count) && Objects.equals(statisticsMetaData, that.statisticsMetaData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, count, statisticsMetaData);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public StatisticsMetaData getStatisticsMetaData() {
        return statisticsMetaData;
    }

    public void setStatisticsMetaData(StatisticsMetaData statisticsMetaData) {
        this.statisticsMetaData = statisticsMetaData;
    }
}
