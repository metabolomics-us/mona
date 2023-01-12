package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsMetaData;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsMetaDataId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

import static org.hibernate.annotations.QueryHints.READ_ONLY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

/**
  * Created by noguzman on 07/28/22.
  */
@Repository("statisticsMetaDataRepository")
@Profile({"mona.persistence"})
public interface StatisticsMetaDataRepository extends JpaRepository<StatisticsMetaData, StatisticsMetaDataId> {
    public StatisticsMetaData findByName(String name);

    @QueryHints(value = {
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query("SELECT new edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsMetaData(s.name, s.count) FROM StatisticsMetaData s")
    public List<StatisticsMetaData> findByProjection();
}
