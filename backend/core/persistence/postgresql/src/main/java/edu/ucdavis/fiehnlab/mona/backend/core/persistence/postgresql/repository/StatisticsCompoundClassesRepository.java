package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics.StatisticsCompoundClasses;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics.StatisticsCompoundClassesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
  * Created by noguzman on 07/28/22.
  */
@Repository("statisticsCompoundClassRepository")
public interface StatisticsCompoundClassesRepository extends JpaRepository<StatisticsCompoundClasses, StatisticsCompoundClassesId> {
    public StatisticsCompoundClasses findByName(String name);
}

