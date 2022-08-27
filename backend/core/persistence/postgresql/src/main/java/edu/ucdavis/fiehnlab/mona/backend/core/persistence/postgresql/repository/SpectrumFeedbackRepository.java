package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumFeedback;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumFeedbackId;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile({"mona.persistence"})
public interface SpectrumFeedbackRepository extends JpaRepository<SpectrumFeedback, SpectrumFeedbackId>{
    List<SpectrumFeedback> findByMonaId(String monaId);

    Long deleteByMonaId(String monaId);

    Long deleteById(Long id);
}
