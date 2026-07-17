package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Tag;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.TagAggregation;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.stream.Stream;

import static org.hibernate.annotations.QueryHints.READ_ONLY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

@Repository
@Profile({"mona.persistence"})
public interface TagsRepository extends JpaRepository<Tag, Long> {
    @QueryHints(value = {
            @QueryHint(name = HINT_FETCH_SIZE, value = "1"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query("select t from Tag t")
    Stream<Tag> streamAllBy();

    // Aggregate the row count of each tag text/ruleBased pair in the database, excluding
    // library tags that have neither a spectrum nor a compound association
    @Query("SELECT new edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.TagAggregation(t.text, t.ruleBased, count(t)) " +
            "FROM Tag t WHERE t.spectrum IS NOT NULL OR t.compound IS NOT NULL GROUP BY t.text, t.ruleBased")
    List<TagAggregation> aggregateTagCounts();

    // Count the distinct tag texts, excluding library tags with no spectrum or compound association
    @Query("SELECT count(DISTINCT t.text) FROM Tag t WHERE t.spectrum IS NOT NULL OR t.compound IS NOT NULL")
    long countDistinctTags();
}
