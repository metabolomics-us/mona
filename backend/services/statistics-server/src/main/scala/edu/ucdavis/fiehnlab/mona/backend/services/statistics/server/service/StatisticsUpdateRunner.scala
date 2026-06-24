package edu.ucdavis.fiehnlab.mona.backend.services.statistics.server.service

import java.util.concurrent.atomic.AtomicBoolean

import edu.ucdavis.fiehnlab.mona.backend.core.statistics.service.StatisticsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

// Runs the statistics recompute on a background thread so the admin endpoint returns immediately
@Component
class StatisticsUpdateRunner {

  @Autowired
  val statisticsService: StatisticsService = null

  // Clears the in-progress flag once the recompute finishes so the next request can be accepted
  @Async
  def runUpdate(inProgress: AtomicBoolean): Unit = {
    try {
      statisticsService.updateStatistics()
    } finally {
      inProgress.set(false)
    }
  }
}
