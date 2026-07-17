package edu.ucdavis.fiehnlab.mona.core.similarity.service

import java.util.concurrent.atomic.AtomicBoolean

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

// Runs the similarity index repopulation on a background thread so the admin endpoint returns immediately
// Kept off the controller so the controller bean is never wrapped in a proxy
@Component
class SimilarityRefreshRunner {

  @Autowired
  val populateService: SimilarityPopulationService = null

  // Clears the in-progress flag once the repopulation finishes so the next request can be accepted
  @Async
  def runRefresh(inProgress: AtomicBoolean): Unit = {
    try {
      populateService.populateIndices()
    } finally {
      inProgress.set(false)
    }
  }
}
