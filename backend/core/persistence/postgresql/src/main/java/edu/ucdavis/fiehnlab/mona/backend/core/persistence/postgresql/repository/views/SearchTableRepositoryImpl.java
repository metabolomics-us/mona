package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.SearchTable;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.SearchTableRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SearchTableRepositoryImpl implements SearchTableRepositoryCustom{
    @Autowired
    SearchTableRepository searchTableRepository;

    @Override
    public List<String> getMonaIdsFromResult(Specification<SearchTable> var1, Class<SearchTableRepository.SparseSearchTable> var2, Pageable var3) {
        List<String> results = new ArrayList<>();
        searchTableRepository.findAll(var1, var2, var3).getContent().forEach( spectrum -> {
            results.add(spectrum.getMonaId());
        });
        return results;
    }

    @Override
    public List<String> getMonaIdsFromResultWithoutPagination(Specification<SearchTable> var1, Class<SearchTableRepository.SparseSearchTable> var2) {
        List<String> results = new ArrayList<>();
        searchTableRepository.findAllWithoutPagination(var1, var2).forEach(spectrum -> {
            results.add(spectrum.getMonaId());
        });
        return results;
    }
}
