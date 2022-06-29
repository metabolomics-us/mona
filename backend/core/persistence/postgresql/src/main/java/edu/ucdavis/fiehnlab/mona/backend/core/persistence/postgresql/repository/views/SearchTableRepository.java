package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.SearchTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import th.co.geniustree.springdata.jpa.repository.JpaSpecificationExecutorWithProjection;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface SearchTableRepository extends JpaRepository<SearchTable, String>, JpaSpecificationExecutorWithProjection<SearchTable, String> {
    public static interface SparseSearchTable {
        String getMonaId();
        String getContent();
    }
}
