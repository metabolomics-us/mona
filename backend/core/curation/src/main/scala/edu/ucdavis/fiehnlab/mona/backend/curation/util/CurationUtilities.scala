package edu.ucdavis.fiehnlab.mona.backend.curation.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Impacts, MetaData, Score, Spectrum}

import scala.collection.mutable.{ArrayBuffer, Buffer}
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 4/20/16.
  */
object CurationUtilities extends LazyLogging {
  final def getCompounds(s: Spectrum, kind: String): Array[Compound] = s.getCompound.asScala.toArray.filter(_.getKind == kind)

  final def getBiologicalCompounds(s: Spectrum): Array[Compound] = getCompounds(s, "biological")

  final def getChemicalCompounds(s: Spectrum): Array[Compound] = getCompounds(s, "chemical")

  final def getPredictedCompounds(s: Spectrum): Array[Compound] = getCompounds(s, "predicted")

  final def getFirstBiologicalCompound(s: Spectrum): Compound = getBiologicalCompounds(s).headOption.orNull

  final def getFirstChemicalCompound(s: Spectrum): Compound = getChemicalCompounds(s).headOption.orNull

  final def getFirstPredictedCompound(s: Spectrum): Compound = getPredictedCompounds(s).headOption.orNull

  /**
    * Find the string value for a given metadata name, or null if not found
    *
    * @param metaData
    * @param name
    * @return
    */
  def findMetaDataValue(metaData: Buffer[MetaData], name: String): String = {
    metaData.filter(_.getName == name) match {
      case x: Buffer[MetaData] if x.nonEmpty => x.head.getValue.toString
      case _ => null
    }
  }

  /**
    *
    * @param score
    * @param impactValue
    * @param impactReason
    * @return
    */
  final def addImpact(score: Score, impactValue: Double, impactReason: String): Score = {
    if (score == null || score.getImpacts == null) {
      val impacts: java.util.List[Impacts] =  Array(new Impacts(impactValue, impactReason)).toList.asJava
      val zero: Double = 0.0
      new Score(impacts, zero, zero, zero)
    } else {
      val impacts: ArrayBuffer[Impacts] = ArrayBuffer[Impacts]()
      impacts.appendAll(score.getImpacts.asScala)
      impacts.append(new Impacts(impactValue, impactReason))
      score.setImpacts(impacts.asJava)
      score
    }
  }
}
