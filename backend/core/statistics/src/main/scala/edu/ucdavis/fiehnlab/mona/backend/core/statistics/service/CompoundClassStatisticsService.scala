package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.lang

import com.mongodb.DBObject
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.{CompoundClassStatisticsMongoRepository, MetaDataStatisticsMongoRepository, TagStatisticsMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{CompoundClassStatistics, TagStatistics}
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 9/27/16.
  */
@Service
class CompoundClassStatisticsService {

  @Autowired
  private val mongoOperations: MongoOperations = null

  @Autowired
  @Qualifier("compoundClassStatisticsMongoRepository")
  private val compoundClassStatisticsRepository: CompoundClassStatisticsMongoRepository = null


  /**
    * Get all data in the compound class statistics repository
    * @return
    */
  def getCompoundClassStatistics: lang.Iterable[CompoundClassStatistics] = compoundClassStatisticsRepository.findAll

  /**
    * Get data for the given compound class from the metadata statistics repository
    * @return
    */
  def getCompoundClassStatistics(compoundClass: String): CompoundClassStatistics = compoundClassStatisticsRepository.findOne(compoundClass)

  /**
    * Count the data in the compound class statistics repository
    * @return
    */
  def countCompoundClassStatistics: Long = compoundClassStatisticsRepository.count()


  /**
    * Collect a list of compound class groups with spectrum and compound counts
    * @return
    */
  def updateCompoundClassStatistics() = {
    val aggregationQuery = newAggregation(
      classOf[Spectrum],
      unwind("$compound"),
      unwind("$compound.metaData"),
      `match`(Criteria.where("compound.metaData.name").is("InChIKey")),
      unwind("$compound.classification"),
      `match`(Criteria.where("compound.classification.name").in("kingdom", "superclass", "class", "subclass")),
      project(bind("InChIKey", "compound.metaData.value"))
        .and("id").as("spectrumId")
        .and("classification").nested(bind("name", "compound.classification.name").and("value", "compound.classification.value")),
      group("spectrumId", "InChIKey")
        .addToSet("classification").as("classifications")
    )

    val results: scala.collection.mutable.Map[String, MutableCompoundClassNode] = scala.collection.mutable.Map[String, MutableCompoundClassNode]()

    mongoOperations
      .aggregate(aggregationQuery, "SPECTRUM", classOf[CompoundClassAggregation])
      .asScala
      .foreach { compoundClassAggregation: CompoundClassAggregation =>
        generateCompoundClassString(compoundClassAggregation).foreach { compoundClass: String =>
          val compoundClassNode: MutableCompoundClassNode = results.getOrElse(compoundClass, new MutableCompoundClassNode(compoundClass))
          compoundClassNode.spectrumCount += 1
          compoundClassNode.compounds += compoundClassAggregation.InChIKey.split("-").head

          results(compoundClass) = compoundClassNode
        }
      }

      results.values.foreach(x =>
        compoundClassStatisticsRepository.save(CompoundClassStatistics(x.name, x.spectrumCount, x.compounds.size)))
  }


  private def generateCompoundClassString(compoundClass: CompoundClassAggregation): Array[String] = {
    val values = Array("kingdom", "superclass", "class", "subclass")
      .map(x => compoundClass.classifications.find(_.name == x).orNull)
      .filter(_ != null)
      .map(_.value)
      .filter(_ != "Chemical entities")

    values.indices.map(i => values.slice(0, i + 1).mkString("|")).toArray
  }
}

case class CompoundClassAggregation(spectrumId: String, InChIKey: String, classifications: Array[CompoundClassEntry])
case class CompoundClassEntry(name: String, value: String)

class MutableCompoundClassNode(val name: String,
                               var spectrumCount: Int = 0,
                               var compounds: scala.collection.mutable.Set[String] = scala.collection.mutable.Set[String]())
