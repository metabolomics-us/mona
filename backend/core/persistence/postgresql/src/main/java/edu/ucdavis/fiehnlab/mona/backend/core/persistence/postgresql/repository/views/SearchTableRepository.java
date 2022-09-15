package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.views.SearchTable;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import th.co.geniustree.springdata.jpa.repository.JpaSpecificationExecutorWithProjection;

import java.util.stream.Stream;

@Repository
@Profile({"mona.persistence"})
public interface SearchTableRepository extends JpaRepository<SearchTable, String>, JpaSpecificationExecutorWithProjection<SearchTable, String>, SearchTableRepositoryCustom {
    public static interface SparseSearchTable {
        String getMonaId();
    }

    public Stream<SearchTable> getAllByMetadataName(String metadataName);

    public Stream<SearchTable> getAllByMetadataCategory(String metadataCategory);
}
