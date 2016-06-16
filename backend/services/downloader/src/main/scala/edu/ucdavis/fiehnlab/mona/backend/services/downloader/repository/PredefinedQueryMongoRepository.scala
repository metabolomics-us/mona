package edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.PredefinedQuery
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 6/6/16.
  */
@Repository("predefinedQueryMongoRepository")
trait PredefinedQueryMongoRepository extends PagingAndSortingRepository[PredefinedQuery, String]