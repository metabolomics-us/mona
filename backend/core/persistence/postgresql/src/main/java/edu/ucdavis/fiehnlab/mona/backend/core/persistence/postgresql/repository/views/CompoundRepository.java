package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.Compound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Stream;

@Repository
public interface CompoundRepository extends JpaRepository<Compound, String>{
    public List<Compound> findByMonaId(String monaId);

    public Stream<Compound> streamAllBy();
}
