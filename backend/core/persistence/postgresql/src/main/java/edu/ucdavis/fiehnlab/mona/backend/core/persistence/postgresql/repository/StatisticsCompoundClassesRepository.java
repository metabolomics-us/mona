package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsCompoundClasses;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsCompoundClassesId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
  * Created by noguzman on 07/28/22.
  */
@Repository("statisticsCompoundClassRepository")
@Profile({"mona.persistence"})
public interface StatisticsCompoundClassesRepository extends JpaRepository<StatisticsCompoundClasses, StatisticsCompoundClassesId> {
    public StatisticsCompoundClasses findByName(String name);
}

