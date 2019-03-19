package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumFeedback
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 06/19/18.
  */
@Repository("spectrumFeedbackMongoRepository")
trait SpectrumFeedbackMongoRepository extends PagingAndSortingRepository[SpectrumFeedback, String]