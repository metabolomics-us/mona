package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
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

//    List<TagDAO> findByMonaId(String monaId);
}
