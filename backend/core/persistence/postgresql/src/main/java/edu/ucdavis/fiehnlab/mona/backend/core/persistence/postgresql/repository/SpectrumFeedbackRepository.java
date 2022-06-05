package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpectrumFeedbackRepository extends JpaRepository<SpectrumFeedback, String>{
    List<SpectrumFeedback> findByMonaId(String monaId);
}
