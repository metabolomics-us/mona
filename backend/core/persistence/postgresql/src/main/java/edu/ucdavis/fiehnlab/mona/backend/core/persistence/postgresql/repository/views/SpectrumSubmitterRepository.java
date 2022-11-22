package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

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

import java.util.stream.Stream;

@Repository
@Profile({"mona.persistence"})
public interface SpectrumSubmitterRepository extends JpaRepository<SpectrumSubmitterStatistics, SpectrumSubmitterStatisticsId> {
    @QueryHints(value = {
            @QueryHint(name = HINT_FETCH_SIZE, value = "50"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query("select s from SpectrumSubmitterStatistics s")
    Stream<SpectrumSubmitterStatistics> streamAllBy();
}
