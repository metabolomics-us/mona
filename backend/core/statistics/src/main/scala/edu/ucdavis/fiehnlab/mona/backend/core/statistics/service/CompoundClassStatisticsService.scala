package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.StatisticsCompoundClassesRepository
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsCompoundClasses
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.CompoundRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.collection.mutable.{ArrayBuffer, Map}
import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters.StreamHasToScala

/**
  * Created by sajjan on 9/27/16.
  */
@Service
@Profile(Array("mona.persistence"))
class CompoundClassStatisticsService extends LazyLogging{
  @Autowired
  private val compoundRepository: CompoundRepository = null

  @Autowired
  private val statisticsCompoundClassesRepository: StatisticsCompoundClassesRepository = null

  /**
    * Get all data in the compound class statistics repository
    *
    * @return
    */
  def getCompoundClassStatistics: Iterable[StatisticsCompoundClasses] = statisticsCompoundClassesRepository.findAll().asScala

  /**
    * Get data for the given compound class from the metadata statistics repository
    *
    * @return
    */
  def getCompoundClassStatistics(name: String): StatisticsCompoundClasses = statisticsCompoundClassesRepository.findByName(name)

  /**
    * Count the data in the compound class statistics repository
    *
    * @return
    */
  def countCompoundClassStatistics: Long = statisticsCompoundClassesRepository.count()

  /**
    * Collect a list of compound class groups with spectrum and compound counts
    *
    * @return
    */
  @Transactional(propagation =  org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  def updateCompoundClassStatistics(): Unit = {
    val finalMap: Map[String, Map[String, ArrayBuffer[String]]] = Map()
    compoundRepository.streamAllBy().toScala(Iterator).foreach { compound =>
      val inchiKeys: ArrayBuffer[String] = ArrayBuffer()
      val compoundClasses: Map[String, String] = Map()
      val compoundClassString: ArrayBuffer[String] = ArrayBuffer()
      compound.getMetaData.asScala.foreach { metadata =>
        if (metadata.getName == "InChIKey") {
          inchiKeys.append(metadata.getValue.substring(0, 14))
        }
      }
      compound.getClassification.asScala.foreach { classification =>
        compoundClasses(classification.getName) = classification.getValue
      }

      if (compoundClasses.contains("kingdom")) {
        if (compoundClasses("kingdom") != "Chemical entities") {
          compoundClassString.append(compoundClasses("kingdom"))
        }
      }
      if (compoundClasses.contains("superclass")) {
        compoundClassString.append(compoundClasses("superclass"))
      }
      if (compoundClasses.contains("class")) {
        compoundClassString.append(compoundClasses("class"))
      }
      if (compoundClasses.contains("subclass")) {
        compoundClassString.append(compoundClasses("subclass"))
      }

      if (compoundClassString.length > 0) {
        for (i <- 0 until compoundClassString.length) {
          val combinedString = compoundClassString.slice(0, i + 1).mkString("|")
          if (!finalMap.contains(combinedString)) {
            finalMap(combinedString) = Map("spectra" -> ArrayBuffer[String](compound.getSpectrum.getId), "compounds" -> inchiKeys)
          } else {
            if (finalMap(combinedString).contains("spectra")) {
              finalMap(combinedString)("spectra").append(compound.getSpectrum.getId)
            }
            if (finalMap(combinedString).contains("compounds")) {
              finalMap(combinedString)("compounds") ++= (inchiKeys)
            }
          }
        }

      }
    }
    finalMap.foreach { case (key, value) =>
      val spectraCount = finalMap(key)("spectra").distinct.length
      val compoundsCount = finalMap(key)("compounds").distinct.length
      val statsCompoundClass = new StatisticsCompoundClasses(key,spectraCount,compoundsCount)
      statisticsCompoundClassesRepository.save(statsCompoundClass)
    }
  }
}

private case class CompoundClassAggregation(_id: String, value: CompoundClassCount)

private case class CompoundClassCount(spectra: Int, compounds: Int)
