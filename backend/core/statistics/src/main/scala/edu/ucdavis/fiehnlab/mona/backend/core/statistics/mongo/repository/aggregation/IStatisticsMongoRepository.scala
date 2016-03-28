package edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.repository.aggregation

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.springframework.data
import org.springframework.stereotype.Repository

/**
  * Created by wohlgemuth on 3/21/16.
  */
@Repository("statisticsMongoRepository")
trait IStatisticsMongoRepository extends data.repository.Repository[Spectrum, String] with StatisticsMongoRepository
