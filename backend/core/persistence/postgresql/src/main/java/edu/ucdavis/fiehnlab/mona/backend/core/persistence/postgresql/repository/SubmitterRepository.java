package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Submitter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import javax.transaction.Transactional;

@Repository
@Profile({"mona.persistence"})
public interface SubmitterRepository extends JpaRepository<Submitter, String> {
    Submitter findTopByEmailAddress(String emailAddress);

    Submitter findByFirstName(String firstName);

    Boolean existsByEmailAddress(String emailAddress);

    @Transactional
    Long deleteByEmailAddress(String emailAddress);
}
