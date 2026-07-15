package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Compound;
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.stream.Stream;

import static org.hibernate.annotations.QueryHints.READ_ONLY;
import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

@Repository
@Profile({"mona.persistence"})
public interface SpectrumRepository extends  JpaRepository<Spectrum, String>, JpaSpecificationExecutor<Spectrum>, SpectrumRepositoryCustom {
    List<Spectrum> findAllByIdIn(List<String> monaIds);

    boolean existsById(String id);

    @QueryHints(value = {
            @QueryHint(name =HINT_FETCH_SIZE, value = "1"),
            @QueryHint(name = HINT_CACHEABLE, value = "false"),
            @QueryHint(name = READ_ONLY, value = "true")
    })
    @Query("select s from Spectrum s")
    Stream<Spectrum> streamAllBy();

    // Projects just the score/splash/submitter/library FK ids for a set of spectra, without loading
    // those entities. Used by tests to capture these ids before a delete so they can confirm the
    // rows are actually gone afterward, since those FK columns live on the spectrum table itself
    @Query("SELECT s.id, score.id, splash.id, submitter.id, library.id FROM Spectrum s " +
            "LEFT JOIN s.score score LEFT JOIN s.splash splash LEFT JOIN s.submitter submitter LEFT JOIN s.library library " +
            "WHERE s.id IN :ids")
    List<Object[]> findAssociationIdsByIdIn(@Param("ids") List<String> ids);
}
