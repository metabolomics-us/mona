package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.Submitter;
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SubmitterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

@Repository
@Profile({"mona.persistence"})
public interface SubmitterRepository extends JpaRepository<Submitter, SubmitterId> {
    Submitter findByEmailAddress(String emailAddress);

    Submitter findByFirstName(String firstName);
}
