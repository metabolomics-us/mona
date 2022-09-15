package edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.PredefinedQuery
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 6/6/16.
  */
@Repository
@Profile(Array("mona.persistence.downloader"))
trait PredefinedQueryRepository extends JpaRepository[PredefinedQuery, String] {

  def findByQuery(query: String): Array[PredefinedQuery]
}
