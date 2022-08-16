package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.Tags;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.TagsId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
@Profile({"mona.persistence"})
public interface TagsRepository extends JpaRepository<Tags, TagsId> {
    Stream<Tags> streamAllBy();

    List<Tags> findByMonaId(String monaId);
}
