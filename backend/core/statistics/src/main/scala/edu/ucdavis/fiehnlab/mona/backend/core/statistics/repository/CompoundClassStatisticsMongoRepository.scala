package edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository

import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.CompoundClassStatistics
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 8/3/16.
  */
@Repository("compoundClassStatisticsMongoRepository")
trait CompoundClassStatisticsMongoRepository extends PagingAndSortingRepository[CompoundClassStatistics, String]