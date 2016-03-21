package edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.data
import org.springframework.stereotype.Repository

@Repository("statisticsMongoRepository")
trait IStatisticsMongoRepository extends data.repository.Repository[Spectrum, String] with StatisticsMongoRepository
