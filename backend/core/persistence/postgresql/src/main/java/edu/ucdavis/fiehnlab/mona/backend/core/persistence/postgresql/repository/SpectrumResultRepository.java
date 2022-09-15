package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResultId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Profile({"mona.persistence"})
public interface SpectrumResultRepository extends  JpaRepository<SpectrumResult, SpectrumResultId> {
    SpectrumResult findByMonaId(String monaId);

    List<SpectrumResult> findAllByMonaIdIn(List<String> monaIds);

    Page<SpectrumResult> findAllByMonaIdIn(List<String> monaIds, Pageable page);

    boolean existsByMonaId(String monaId);

    @Transactional
    void deleteByMonaId(String monaId);
}
