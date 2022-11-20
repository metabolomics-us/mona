package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.SubmitterDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import javax.transaction.Transactional;

@Repository
@Profile({"mona.persistence"})
public interface SubmitterRepository extends JpaRepository<SubmitterDAO, String> {
    SubmitterDAO findTopByEmailAddress(String emailAddress);

    SubmitterDAO findByFirstName(String firstName);

    Boolean existsByEmailAddress(String emailAddress);

    @Transactional
    Long deleteByEmailAddress(String emailAddress);
}
