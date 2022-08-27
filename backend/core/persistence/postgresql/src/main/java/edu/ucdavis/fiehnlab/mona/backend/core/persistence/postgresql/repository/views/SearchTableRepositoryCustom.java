package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.SearchTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface SearchTableRepositoryCustom {
    public List<String> getMonaIdsFromResult(Specification<SearchTable> var1, Class<SearchTableRepository.SparseSearchTable> var2, Pageable var3);

    public List<String> getMonaIdsFromResultWithoutPagination(Specification<SearchTable> var, Class<SearchTableRepository.SparseSearchTable> var2);
}
