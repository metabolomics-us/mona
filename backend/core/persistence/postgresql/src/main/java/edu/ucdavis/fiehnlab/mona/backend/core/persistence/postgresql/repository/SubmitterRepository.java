package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.Submitter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmitterRepository extends JpaRepository<Submitter, String> {
    Submitter findByEmailAddress(String emailAddress);

    Submitter findByFirstName(String firstName);
}
