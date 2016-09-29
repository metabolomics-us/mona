package edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository

import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.GlobalStatistics
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 9/26/16.
  */
@Repository("globalStatisticsMongoRepository")
trait GlobalStatisticsMongoRepository extends PagingAndSortingRepository[GlobalStatistics, String]