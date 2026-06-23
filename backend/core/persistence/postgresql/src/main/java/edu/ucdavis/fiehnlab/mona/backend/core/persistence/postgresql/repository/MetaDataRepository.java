package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.MetaData;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.MetaDataValueAggregation;
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
public interface MetaDataRepository extends JpaRepository<MetaData, Long> {
//    List<MetaDataDAO> findBySpectrumMetadataId(String spectrum_metadata_id);

    @QueryHints(value = {
            @QueryHint(name =HINT_FETCH_SIZE, value = "1"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query("select m from MetaData m")
    public Stream<MetaData> streamAllBy();

    // Aggregate the row count of every distinct metadata name/value pair in the database
    @QueryHints(value = {
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query("SELECT new edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.MetaDataValueAggregation(m.name, m.value, count(m)) " +
            "FROM MetaData m GROUP BY m.name, m.value")
    public List<MetaDataValueAggregation> aggregateValueCounts();

    // Count the distinct metadata names directly in the database
    @Query("SELECT count(DISTINCT m.name) FROM MetaData m")
    public long countDistinctNames();

    // Count the distinct InChIKey first blocks (first 14 characters) across all compound metadata
    @Query("SELECT count(DISTINCT substring(m.value, 1, 14)) FROM MetaData m WHERE m.name = 'InChIKey' AND m.compoundMetadata IS NOT NULL")
    public long countDistinctCompoundInchiKeyBlocks();
}
