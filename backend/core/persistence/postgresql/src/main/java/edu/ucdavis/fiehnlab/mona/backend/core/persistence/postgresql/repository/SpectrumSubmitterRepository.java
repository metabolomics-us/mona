package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.SubmitterAggregation;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.views.SpectrumSubmitterStatistics;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.views.SpectrumSubmitterStatisticsId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import javax.persistence.QueryHint;

import static org.hibernate.annotations.QueryHints.READ_ONLY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

import java.util.List;
import java.util.stream.Stream;

@Repository
@Profile({"mona.persistence"})
public interface SpectrumSubmitterRepository extends JpaRepository<SpectrumSubmitterStatistics, SpectrumSubmitterStatisticsId> {
    @QueryHints(value = {
            @QueryHint(name =HINT_FETCH_SIZE, value = "1"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query("select s from SpectrumSubmitterStatistics s")
    Stream<SpectrumSubmitterStatistics> streamAllBy();

    // Aggregate submitter statistics grouped by email address in the database,
    // averaging the score and counting the submissions
    @Query("SELECT new edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.SubmitterAggregation(" +
            "s.emailAddress, min(s.firstName), min(s.lastName), min(s.institution), count(s), avg(s.score)) " +
            "FROM SpectrumSubmitterStatistics s GROUP BY s.emailAddress")
    List<SubmitterAggregation> aggregateSubmitterStatistics();

    // Count the distinct submitter email addresses directly in the database
    @Query("SELECT count(DISTINCT s.emailAddress) FROM SpectrumSubmitterStatistics s")
    long countDistinctEmailAddresses();
}
