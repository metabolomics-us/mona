package edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository

import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.CompoundMetaDataStatistics
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository("compoundMetadataStatisticsMongoRepository")
trait CompoundMetaDataStatisticsMongoRepository extends PagingAndSortingRepository[CompoundMetaDataStatistics, String]{

}
