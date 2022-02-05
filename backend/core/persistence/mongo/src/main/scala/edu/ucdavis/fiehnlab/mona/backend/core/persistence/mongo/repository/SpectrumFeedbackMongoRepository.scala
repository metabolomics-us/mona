package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumFeedback
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype

/**
  * Created by sajjan on 06/19/18.
  */
@stereotype.Repository("spectrumFeedbackMongoRepository")
trait SpectrumFeedbackMongoRepository extends PagingAndSortingRepository[SpectrumFeedback , String]{
  /**
   * returns all feedback with this Mona ID
   *
   * @param monaID
   * @return
   */
  def findByMonaID(monaID: String): java.lang.Iterable[SpectrumFeedback]
}
