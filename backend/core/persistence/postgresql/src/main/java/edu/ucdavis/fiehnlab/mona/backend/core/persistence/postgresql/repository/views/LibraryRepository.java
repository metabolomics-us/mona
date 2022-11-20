package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.LibraryDAO;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
@Profile({"mona.persistence"})
public interface LibraryRepository extends JpaRepository<LibraryDAO, Long> {
    Stream<LibraryDAO> streamAllBy();

    Boolean existsByTag_Text(String text);

}
