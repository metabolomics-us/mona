package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.{Submitter, Spectrum}
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 3/9/16.
  */
@Repository("submitterMongoRepository")
trait ISubmitterMongoRepository extends PagingAndSortingRepository[Submitter, String]