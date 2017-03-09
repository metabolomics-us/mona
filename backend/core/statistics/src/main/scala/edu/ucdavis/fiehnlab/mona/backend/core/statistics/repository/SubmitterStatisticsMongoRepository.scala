package edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository

import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.SubmitterStatistics
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 8/3/16.
  */
@Repository("submitterStatisticsMongoRepository")
trait SubmitterStatisticsMongoRepository extends PagingAndSortingRepository[SubmitterStatistics, String]