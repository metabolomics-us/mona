package edu.ucdavis.fiehnlab.mona.backend.core.curation.service

import java.util.concurrent.atomic.AtomicBoolean

import com.typesafe.scalalogging.Logger
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

// Runs the mass curation scheduling loop on a background thread so the admin endpoint returns immediately
// Kept off the controller so the controller bean is never wrapped in a proxy
// Extends no trait on purpose: @Async proxies an interface-bearing bean as a JDK dynamic proxy,
// which would not be assignable to the concrete CurationRunner type the controller autowires
@Component
class CurationRunner {

  private val logger = Logger[CurationRunner]

  @Autowired
  val curationService: CurationService = null

  // Number of spectra loaded and scheduled per transaction while walking the table
  private val PageSize: Int = 1000

  /**
    * Pages through every spectrum matching the given query and schedules each for curation
    * Delegates one page at a time to CurationService.scheduleSpectraPage so each page runs in its
    * own read-only transaction (needed for lazy serialization) rather than one transaction per run
    * Clears the in-progress flag once every spectrum has been queued so the next request can be accepted
    *
    * @param query
    * @param inProgress
    */
  @Async
  def scheduleAllForCuration(query: String, inProgress: AtomicBoolean): Unit = {
    try {
      var pageNumber: Int = 0
      var count: Int = 0
      var hasNext: Boolean = true

      while (hasNext) {
        val page: Page[Spectrum] = curationService.scheduleSpectraPage(query, PageRequest.of(pageNumber, PageSize))
        count += page.getNumberOfElements
        hasNext = page.hasNext
        pageNumber += 1

        if (pageNumber % 10 == 0) {
          logger.info(s"Scheduled $count spectra...")
        }
      }

      logger.info(s"Finished scheduling $count spectra")
    } finally {
      inProgress.set(false)
    }
  }
}
