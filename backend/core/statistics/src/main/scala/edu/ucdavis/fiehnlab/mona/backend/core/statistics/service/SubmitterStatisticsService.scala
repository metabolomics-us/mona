package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import edu.ucdavis.fiehnlab.mona.backend.core.domain.views.SpectraSubmitters
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsSubmitter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.StatisticsSubmitterRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.SpectraSubmittersRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.collection.mutable.{ListBuffer, Map}
import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters.StreamHasToScala

@Service
class SubmitterStatisticsService {
  @Autowired
  val statisticsSubmitterRepository: StatisticsSubmitterRepository = null

  @Autowired
  val spectraSubmittersRepository: SpectraSubmittersRepository = null


  @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def updateSubmitterStatistics(): Unit = {
    statisticsSubmitterRepository.deleteAll()
    val submitterObjects: Map[String, SpectraSubmitters] = Map()
    val submitterCounter: Map[String, Integer] = Map()
    val submitterScores: Map[String, ListBuffer[Double]] = Map()
    spectraSubmittersRepository.streamAllBy().toScala(Iterator).foreach { submitter =>
      if (submitterObjects.contains(submitter.getEmailAddress)) {
        submitterCounter(submitter.getEmailAddress) += 1
        submitterScores(submitter.getEmailAddress).append(submitter.getScore)
      } else {
        submitterObjects(submitter.getEmailAddress) = submitter
        submitterCounter(submitter.getEmailAddress) = 1
        submitterScores(submitter.getEmailAddress) = ListBuffer[Double](submitter.getScore)
      }
    }
    submitterObjects.foreach { case (key, value) =>
      val averagedScore = submitterScores(key).sum / submitterScores(key).length
      val entry = new StatisticsSubmitter(value.getEmailAddress, value.getFirstName, value.getLastName, value.getInstitution, submitterCounter(key), averagedScore)
      statisticsSubmitterRepository.save(entry)
    }
  }
  /**
   * Get all data in the submitter statistics repository
   *
   * @return
   * */
  def getSubmitterStatistics: Iterable[StatisticsSubmitter] = statisticsSubmitterRepository.findByOrderByScoreDesc().asScala
}
