package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpectrumResultRepository extends JpaRepository<SpectrumResult, String>{
    SpectrumResult findByMonaId(String monaId);

    boolean existsByMonaId(String monaId);

    void deleteByMonaId(String monaId);
}
