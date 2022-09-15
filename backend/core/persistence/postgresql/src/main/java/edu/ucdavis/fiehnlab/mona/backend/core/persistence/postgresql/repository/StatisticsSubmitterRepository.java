package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsSubmitter;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsSubmitterId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

/**
  * Created by noguzman on 07/28/22.
  */
@Repository("statisticsSubmitterRepository")
@Profile({"mona.persistence"})
public interface StatisticsSubmitterRepository extends JpaRepository<StatisticsSubmitter, StatisticsSubmitterId> {
    public Stream<StatisticsSubmitter> streamAllBy();

    public List<StatisticsSubmitter> findByOrderByScoreDesc();
}
