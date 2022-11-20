package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.CompoundDAO;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.stream.Stream;

@Repository
@Profile({"mona.persistence"})
public interface CompoundRepository extends JpaRepository<CompoundDAO, String>{
//    public List<CompoundDAO> findBySpectrumId(String spectrumId);

    public Stream<CompoundDAO> streamAllBy();
}
