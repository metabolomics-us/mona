package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics.StatisticsMetaData.StatisticsMetaDataSummary;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics.StatisticsMetaData;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics.StatisticsMetaDataId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
  * Created by noguzman on 07/28/22.
  */
@Repository("statisticsMetaDataRepository")
@Profile({"mona.persistence"})
public interface StatisticsMetaDataRepository extends JpaRepository<StatisticsMetaData, StatisticsMetaDataId> {
    public StatisticsMetaData findByName(String name);

    public List<StatisticsMetaDataSummary> findBy();
}
