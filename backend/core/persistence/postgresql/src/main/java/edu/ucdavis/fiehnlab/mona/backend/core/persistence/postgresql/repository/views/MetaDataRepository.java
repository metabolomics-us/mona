package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.views.MetaData;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.views.MetaDataId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

@Repository
@Profile({"mona.persistence"})
public interface MetaDataRepository extends JpaRepository<MetaData, MetaDataId> {
    List<MetaData> findByMonaId(String monaId);

    public Stream<MetaData> streamAllBy();
}
