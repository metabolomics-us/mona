package edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.repository.aggregation

import java.util.LinkedHashMap

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.aggregation.TypedAggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository
import scala.collection.JavaConverters._

@Repository
class IStatisticsMongoRepositoryImpl extends StatisticsMongoRepository with LazyLogging {

  @Autowired
  val mongoOperations: MongoOperations = null

  /**
    * Return the aggregation of metadata values for a given field name, sorted by highest number
    * of occurrences. By default, this method inspects fields from the base metadata group; to
    * specify custom values, supply a `metaDataGroup` value.
    *
    * @param name          The target metadata field
    * @param metaDataGroup The metadata group to inspect
    * @return
    */
  def aggregateByName(name: String, metaDataGroup: Option[String] = None): Seq[(AnyVal, Int)] = {
    if (name == null) throw new IllegalArgumentException("Metadata field name must not be null")
    // Null checking for numResults and metaDataGroup is checked by the compiler, and
    // positive long values for numResults is checked by the aggregation framework

    val aggregationQuery: TypedAggregation[Spectrum] = metaDataGroup.map {
      kind =>
        newAggregation(
          classOf[Spectrum],
          project("compound"),
          unwind("compound"),
          `match`(Criteria.where("compound.kind").is(kind)),
          unwind("compound.metaData"),
          `match`(Criteria.where("compound.metaData.name").is(name)),
          project(bind("value", "compound.metaData.value")),
          group("value").count().as("total"),
          project("total").and("value").previousOperation(),
          sort(Sort.Direction.ASC, "value"),
          sort(Sort.Direction.DESC, "total"))
    } getOrElse {
      newAggregation(
        classOf[Spectrum],
        project("metaData"),
        unwind("metaData"),
        `match`(Criteria.where("metaData.name").is(name)),
        project(bind("value", "metaData.value")),
        group("value").count().as("total"),
        project("total").and("value").previousOperation(),
        sort(Sort.Direction.ASC, "value"),
        sort(Sort.Direction.DESC, "total"))
    }

    val results = mongoOperations.aggregate(aggregationQuery, classOf[Spectrum],
      classOf[LinkedHashMap[String, Object]]).getMappedResults.asScala

    val typedResults = results.map(x => (asScalaType(x.get("value")), x.get("total").asInstanceOf[Int]))

    typedResults
  }

  def asScalaType(obj: Object): AnyVal = obj match {
    case o: java.lang.Integer => o.toInt
    case o: java.lang.Double => o.toDouble
    case o: String => o.asInstanceOf[AnyVal]
  }
}
