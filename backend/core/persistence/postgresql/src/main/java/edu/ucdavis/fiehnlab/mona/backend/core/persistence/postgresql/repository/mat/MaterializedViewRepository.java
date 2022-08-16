package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.mat;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResultId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@Profile({"mona.persistence"})
public interface MaterializedViewRepository extends Repository<SpectrumResult, SpectrumResultId> {
    @Modifying
    @Transactional
    @Query(value = "refresh materialized view search_table_mat", nativeQuery = true)
    void refreshSearchTable();
}
