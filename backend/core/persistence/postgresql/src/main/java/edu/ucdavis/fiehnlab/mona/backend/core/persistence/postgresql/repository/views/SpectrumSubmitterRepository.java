package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.views.SpectrumSubmitterStatistics;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.views.SpectrumSubmitterStatisticsId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
@Profile({"mona.persistence"})
public interface SpectrumSubmitterRepository extends JpaRepository<SpectrumSubmitterStatistics, SpectrumSubmitterStatisticsId> {
    Stream<SpectrumSubmitterStatistics> streamAllBy();
}
