package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsSubmitter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.views.SpectrumSubmitterStatistics
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SpectrumSubmitterRepository, StatisticsSubmitterRepository}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

import scala.collection.mutable.{ListBuffer, Map}
import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters.StreamHasToScala

@Service
@Profile(Array("mona.persistence"))
class SubmitterStatisticsService extends LazyLogging{
  @Autowired
  val statisticsSubmitterRepository: StatisticsSubmitterRepository = null

  @Autowired
  val spectraSubmittersRepository: SpectrumSubmitterRepository = null

  @Autowired
  private val entityManager: EntityManager = null

  def updateSubmitterStatisticsHelper(): (Map[String, SpectrumSubmitterStatistics], Map[String, Integer], Map[String, ListBuffer[Double]]) = {
    val submitterObjects: Map[String, SpectrumSubmitterStatistics] = Map()
    val submitterCounter: Map[String, Integer] = Map()
    val submitterScores: Map[String, ListBuffer[Double]] = Map()
    var counter = 0

    spectraSubmittersRepository.streamAllBy().toScala(Iterator).foreach { submitter =>
      if (submitterObjects.contains(submitter.getEmailAddress)) {
        submitterCounter(submitter.getEmailAddress) += 1
        submitterScores(submitter.getEmailAddress).append(submitter.getScore)
      } else {
        submitterObjects(submitter.getEmailAddress) = submitter
        submitterCounter(submitter.getEmailAddress) = 1
        submitterScores(submitter.getEmailAddress) = ListBuffer[Double](submitter.getScore)
      }
      counter += 1

      if (counter % 100000 == 0) {
        logger.info(s"\tCompleted Submitter Object #${counter}")
        entityManager.flush()
        entityManager.clear()
      }
      entityManager.detach(submitter)
    }
    (submitterObjects, submitterCounter, submitterScores)
  }

  @Transactional
  def updateSubmitterStatistics(): String = {
    statisticsSubmitterRepository.deleteAll()
    val (submitterObjects, submitterCounter, submitterScores) = updateSubmitterStatisticsHelper()

    submitterObjects.foreach { case (key, value) =>
      val averagedScore = submitterScores(key).sum / submitterScores(key).length
      val entry = new StatisticsSubmitter(value.getEmailAddress, value.getFirstName, value.getLastName, value.getInstitution, submitterCounter(key), averagedScore)
      statisticsSubmitterRepository.save(entry)
      entityManager.detach(entry)
    }
    submitterObjects.clear()
    submitterCounter.clear()
    submitterScores.clear()
    "Submitter Statistics Completed"
  }
  /**
   * Get all data in the submitter statistics repository
   *
   * @return
   * */
  def getSubmitterStatistics: Iterable[StatisticsSubmitter] = statisticsSubmitterRepository.findByOrderByScoreDesc().asScala
}
