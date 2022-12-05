package edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@IdClass(StatisticsMetaDataId.class)
@Table(name = "statistics_metadata")
@Profile({"mona.persistence"})
public class StatisticsMetaData implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "statistics_metadata_id")
    @SequenceGenerator(name = "statistics_metadata_id", initialValue = 1, allocationSize = 50)
    private Long id;

    @Id
    private String name;

    private Integer count;


    @OneToMany(fetch = FetchType.EAGER, targetEntity = MetaDataValueCount.class, cascade = CascadeType.ALL)
    @OrderBy("count DESC")
    @JoinColumn(name = "statistics_metadata_id", referencedColumnName = "id")
    @JoinColumn(name = "statistics_metadata_name", referencedColumnName = "name")
    @Column(name = "metadata_value_count", nullable = false)
    private List<MetaDataValueCount> metaDataValueCount;

    public StatisticsMetaData() {}

    public StatisticsMetaData(String name, Integer count, List<MetaDataValueCount> metaDataValueCount) {
        this.name = name;
        this.count = count;
        this.metaDataValueCount = metaDataValueCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<MetaDataValueCount> getMetaDataValueCount() {
        return metaDataValueCount;
    }

    public void setMetaDataValueCount(List<MetaDataValueCount> metaDataValueCount) {
        this.metaDataValueCount = metaDataValueCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatisticsMetaData that = (StatisticsMetaData) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(count, that.count) && Objects.equals(metaDataValueCount, that.metaDataValueCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, count, metaDataValueCount);
    }

    public interface StatisticsMetaDataSummary{
        //@Value annotations needed because of a bug with projections and JPA in 2.6.3, is fixed in 2.7(potentially 2.6.4)
        //can remove the annotations when updated https://github.com/spring-projects/spring-data-jpa/issues/2408
        @Value("#{target.Name}")
        String getName();
        @Value("#{target.Count}")
        Integer getCount();
    }
}
