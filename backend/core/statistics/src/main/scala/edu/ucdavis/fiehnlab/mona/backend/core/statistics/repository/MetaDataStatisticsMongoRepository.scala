package edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository

import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.MetaDataStatistics
import org.springframework.data
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 8/3/16.
  */
@Repository("metadataStatisticsMongoRepository")
trait MetaDataStatisticsMongoRepository extends PagingAndSortingRepository[MetaDataStatistics, String]