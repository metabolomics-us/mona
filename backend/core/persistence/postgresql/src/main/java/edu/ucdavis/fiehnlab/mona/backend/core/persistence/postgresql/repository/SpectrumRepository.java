package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile({"mona.persistence"})
public interface SpectrumRepository extends  JpaRepository<Spectrum, String>, JpaSpecificationExecutor<Spectrum> {
    List<Spectrum> findAllByIdIn(List<String> monaIds);

    Page<Spectrum> findAllByIdIn(List<String> monaIds, Pageable page);

    boolean existsById(String id);
}
