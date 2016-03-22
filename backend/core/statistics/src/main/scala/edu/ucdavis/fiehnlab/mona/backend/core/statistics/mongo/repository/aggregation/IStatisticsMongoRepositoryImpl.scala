package edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.repository.aggregation

import java.util.LinkedHashMap

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

import scala.collection.JavaConverters._

@Repository
class IStatisticsMongoRepositoryImpl extends StatisticsMongoRepository with LazyLogging {

  @Autowired
  val mongoOperations: MongoOperations = null

  def aggregateByName(name: String, metaDataGroup: Option[String] = None): Seq[(AnyVal, Int)] = {
    if (name == null) throw new IllegalArgumentException("Metadata field must not be null")
    // Null checking for numResults and metaDataGroup is checked by the compiler, and
    // positive long values for numResults is checked by the aggregation framework

    val metaData = metaDataGroup map (_ + ".metaData") getOrElse "metaData"

    val aggregationQuery = newAggregation(classOf[Spectrum],
      project(metaData),
      unwind("metaData"),
      `match`(Criteria.where("metaData.name").is(name)),
      project("metaData.value"),
      group("value").count().as("total"),
      project("total").and("value").previousOperation(),
      sort(Sort.Direction.ASC, "value"),
      sort(Sort.Direction.DESC, "total"))

    val results = mongoOperations.aggregate(aggregationQuery, classOf[Spectrum],
      classOf[LinkedHashMap[String, Object]]).getMappedResults.asScala

    val typedResults = results.map(x => (x.get("value").asInstanceOf[AnyVal], x.get("total").asInstanceOf[Int]))
    typedResults
  }
}
