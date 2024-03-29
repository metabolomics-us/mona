package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsTag;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsTagId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
  * Created by sajjan on 8/3/16.
  */
@Repository("statisticsTagRepository")
@Profile({"mona.persistence"})
public interface StatisticsTagRepository extends JpaRepository<StatisticsTag, StatisticsTagId> {

  List<StatisticsTag> findByText(String text);

  List<StatisticsTag> findByCategoryOrderByCountDesc(String category);

  List<StatisticsTag> findByOrderByCountDesc();
}
