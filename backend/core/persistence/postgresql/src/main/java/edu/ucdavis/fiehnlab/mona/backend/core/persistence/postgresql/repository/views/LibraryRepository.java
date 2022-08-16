package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.Library;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.LibraryId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
@Profile({"mona.persistence"})
public interface LibraryRepository extends JpaRepository<Library, LibraryId> {
    Stream<Library> streamAllBy();

    Library findByMonaId(String monaId);

    Boolean existsByText(String text);
}
