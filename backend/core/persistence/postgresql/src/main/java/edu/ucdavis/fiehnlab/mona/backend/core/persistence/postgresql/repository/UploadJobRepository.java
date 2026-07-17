package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.UploadJob;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
@Profile({"mona.persistence"})
public interface UploadJobRepository extends JpaRepository<UploadJob, String> {

    // Upload history for a single user, newest first
    List<UploadJob> findByEmailAddressOrderByDateDesc(String emailAddress);

    // Used by the reconcile routine to find jobs orphaned by a crashed worker
    List<UploadJob> findByStatus(String status);

    // Used by the sweep to find UPLOADING jobs whose last chunk activity is stale
    List<UploadJob> findByStatusAndLastUpdatedBefore(String status, Date threshold);
}
