package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.NewsEntry
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 04/05/17.
  */
@Repository("newsMongoRepository")
trait NewsMongoRepository extends PagingAndSortingRepository[NewsEntry, String]