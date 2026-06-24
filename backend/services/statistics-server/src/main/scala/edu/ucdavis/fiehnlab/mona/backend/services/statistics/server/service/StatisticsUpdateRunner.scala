package edu.ucdavis.fiehnlab.mona.backend.services.statistics.server.service

import edu.ucdavis.fiehnlab.mona.backend.core.statistics.service.StatisticsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

// Runs the statistics recompute on a background thread so the admin endpoint returns immediately
@Component
class StatisticsUpdateRunner {

  @Autowired
  val statisticsService: StatisticsService = null

  @Async
  def runUpdate(): Unit = statisticsService.updateStatistics()
}
