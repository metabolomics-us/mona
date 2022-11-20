package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.MetaDataDAO;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
@Profile({"mona.persistence"})
public interface MetaDataRepository extends JpaRepository<MetaDataDAO, Long> {
//    List<MetaDataDAO> findBySpectrumMetadataId(String spectrum_metadata_id);

    public Stream<MetaDataDAO> streamAllBy();
}
