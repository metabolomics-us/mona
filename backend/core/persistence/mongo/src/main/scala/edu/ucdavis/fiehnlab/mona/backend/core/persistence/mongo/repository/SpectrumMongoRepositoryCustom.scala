package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import org.springframework.data.mongodb.core.query.Query

/**
  * Created by wohlgemuth on 2/26/16.
  */
trait SpectrumMongoRepositoryCustom extends RSQLRepositoryCustom[Spectrum,Query]