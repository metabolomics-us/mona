package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by wohlgemuth on 2/26/16.
  */
@Repository("spectrumMongoRepository")
trait ISpectrumMongoRepositoryCustom extends PagingAndSortingRepository[Spectrum, String]
  with SpectrumMongoRepositoryCustom
  with MetadataMongoRepositoryCustom
