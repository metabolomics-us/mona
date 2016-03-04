package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.{Sort, Pageable, Page}
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.{PagingAndSortingRepository, CrudRepository}

/**
  * Created by wohlgemuth on 2/26/16.
  */
trait ISpectrumMongoRepositoryCustom extends PagingAndSortingRepository[Spectrum, String] with SpectrumMongoRepositoryCustom
