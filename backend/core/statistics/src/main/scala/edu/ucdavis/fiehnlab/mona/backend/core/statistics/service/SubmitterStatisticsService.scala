package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsSubmitter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SpectrumSubmitterRepository, StatisticsSubmitterRepository}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager
import scala.jdk.CollectionConverters._

@Service
@Profile(Array("mona.persistence"))
class SubmitterStatisticsService extends LazyLogging{
  @Autowired
  val statisticsSubmitterRepository: StatisticsSubmitterRepository = null

  @Autowired
  val spectraSubmittersRepository: SpectrumSubmitterRepository = null

  @Autowired
  private val entityManager: EntityManager = null

  @Transactional
  def updateSubmitterStatistics(): String = {
    logger.info("Aggregating submitter statistics now...")
    val start = System.currentTimeMillis()
    statisticsSubmitterRepository.deleteAllInBatch()

    // Aggregate per submitter counts and average scores in the database, grouped by email address
    val aggregations = spectraSubmittersRepository.aggregateSubmitterStatistics().asScala
    aggregations.foreach { aggregation =>
      val entry = new StatisticsSubmitter(aggregation.getEmailAddress, aggregation.getFirstName,
        aggregation.getLastName, aggregation.getInstitution, aggregation.getCount.toInt, aggregation.getScore)
      statisticsSubmitterRepository.save(entry)
      entityManager.detach(entry)
    }
    entityManager.flush()
    entityManager.clear()
    logger.info(f"Submitter statistics complete: ${aggregations.size} submitters in ${(System.currentTimeMillis() - start) / 1000.0}%.2fs")
    "Submitter Statistics Completed"
  }
  /**
   * Get all data in the submitter statistics repository
   *
   * @return
   * */
  def getSubmitterStatistics: Iterable[StatisticsSubmitter] = statisticsSubmitterRepository.findByOrderByScoreDesc().asScala
}
