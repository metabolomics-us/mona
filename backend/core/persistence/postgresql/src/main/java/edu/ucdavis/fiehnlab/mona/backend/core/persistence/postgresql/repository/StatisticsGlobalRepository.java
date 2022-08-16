package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics.StatisticsGlobal;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
  * Created by noguzman on 07/28/22.
  */
@Repository("statisticsGlobalRepository")
@Profile({"mona.persistence"})
public interface StatisticsGlobalRepository extends JpaRepository<StatisticsGlobal, Long> {

}
