package edu.ucdavis.fiehnlab.mona.backend.curation.util

import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao._
import scala.collection.mutable.{Buffer, ArrayBuffer}
import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 4/20/16.
  */
object CurationUtilities {
  final def getCompounds(s: Spectrum, kind: String): Array[CompoundDAO] = s.getCompound.asScala.toArray.filter(_.getKind == kind)

  final def getBiologicalCompounds(s: Spectrum): Array[CompoundDAO] = getCompounds(s, "biological")

  final def getChemicalCompounds(s: Spectrum): Array[CompoundDAO] = getCompounds(s, "chemical")

  final def getPredictedCompounds(s: Spectrum): Array[CompoundDAO] = getCompounds(s, "predicted")

  final def getFirstBiologicalCompound(s: Spectrum): CompoundDAO = getBiologicalCompounds(s).headOption.orNull

  final def getFirstChemicalCompound(s: Spectrum): CompoundDAO = getChemicalCompounds(s).headOption.orNull

  final def getFirstPredictedCompound(s: Spectrum): CompoundDAO = getPredictedCompounds(s).headOption.orNull

  /**
    * Find the string value for a given metadata name, or null if not found
    *
    * @param metaData
    * @param name
    * @return
    */
  def findMetaDataValue(metaData: Buffer[MetaDataDAO], name: String): String = {
    metaData.filter(_.getName == name) match {
      case x: Buffer[MetaDataDAO] if x.nonEmpty => x.head.getValue.toString
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
      val impacts: java.util.List[Impacts] =  List(new Impacts(impactValue, impactReason)).asJava
      val zero: Double = 0.0
      new Score(impacts, zero, zero, zero)
    } else {
      val impacts: java.util.List[Impacts] = score.getImpacts
      impacts.add(new Impacts(impactValue, impactReason))
      score.setImpacts(impacts)
      score
    }
  }
}
