package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumFeedbackId;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface SpectrumResultRepository extends JpaRepository<SpectrumResult, SpectrumFeedbackId> {
    SpectrumResult findByMonaId(String monaId);

    boolean existsByMonaId(String monaId);

    @Transactional
    void deleteByMonaId(String monaId);
}
