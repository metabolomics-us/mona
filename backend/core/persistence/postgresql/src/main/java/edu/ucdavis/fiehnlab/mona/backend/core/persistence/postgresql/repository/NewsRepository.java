package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository;

import edu.ucdavis.fiehnlab.mona.backend.core.domain.News;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Profile({"mona.persistence"})
public interface NewsRepository extends JpaRepository<News, Long>{
}
