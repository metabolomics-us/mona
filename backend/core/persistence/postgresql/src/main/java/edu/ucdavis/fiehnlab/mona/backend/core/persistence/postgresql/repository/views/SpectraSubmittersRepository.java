package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.SpectraSubmitters;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.SpectraSubmittersId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
@Profile({"mona.persistence"})
public interface SpectraSubmittersRepository extends JpaRepository<SpectraSubmitters, SpectraSubmittersId> {
    Stream<SpectraSubmitters> streamAllBy();
}
