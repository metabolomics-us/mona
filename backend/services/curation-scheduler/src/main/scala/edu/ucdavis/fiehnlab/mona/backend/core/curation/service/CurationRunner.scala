package edu.ucdavis.fiehnlab.mona.backend.core.curation.service

import java.util.concurrent.atomic.AtomicBoolean

import com.typesafe.scalalogging.Logger
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.springframework.beans.factory.annotation.Autowired
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

  // Number of spectra loaded and scheduled per keyset page while walking the table
  private val PageSize: Int = 1000

  /**
    * Walks every spectrum matching the given query with a keyset cursor and schedules each for curation
    * Delegates one page at a time to CurationService.scheduleSpectraKeysetPage so each page runs in its
    * own read-only transaction (needed for lazy serialization) and the cursor avoids a growing offset
    * Clears the in-progress flag once every spectrum has been queued so the next request can be accepted
    *
    * @param query
    * @param inProgress
    */
  @Async
  def scheduleAllForCuration(query: String, inProgress: AtomicBoolean): Unit = {
    try {
      var lastId: String = null
      var count: Int = 0
      var pageIndex: Int = 0
      var hasNext: Boolean = true

      while (hasNext) {
        val page: java.util.List[Spectrum] = curationService.scheduleSpectraKeysetPage(query, lastId, PageSize)
        val pageSize: Int = page.size

        if (pageSize > 0) {
          count += pageSize
          lastId = page.get(pageSize - 1).getId
          pageIndex += 1

          if (pageIndex % 10 == 0) {
            logger.info(s"Scheduled $count spectra...")
          }
        }

        // A short (or empty) page means the cursor reached the end of the table
        hasNext = pageSize == PageSize
      }

      logger.info(s"Finished scheduling $count spectra")
    } finally {
      inProgress.set(false)
    }
  }
}
