package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller

import java.util
import java.util.concurrent.Future

import com.mongodb.DBObject
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.ISpectrumMongoRepositoryCustom
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.scheduling.annotation.{AsyncResult, Async}
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, RequestMethod, RestController}
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.query.{Criteria, BasicQuery}
import scala.collection.JavaConverters._

/**
  * Created by wohlg_000 on 3/7/2016.
  */
@RestController
@RequestMapping(Array("/rest/metaData"))
class MetaDataRestController {


  @Autowired
  val mongoOperations: MongoOperations = null


  /**
    * this is the utilized repository, doing all the heavy lifting
    */
  @Autowired
  val spectrumRepository: ISpectrumMongoRepositoryCustom = null


  @RequestMapping(path = Array("/names"), method = Array(RequestMethod.GET))
  @Async
  def listMetaDataName: Future[java.util.List[String]] = {

    val aggregations = newAggregation(
      unwind("$metaData"),
      group("metaData.name")
    )

    val result:List[String] = mongoOperations.aggregate(aggregations, "SPECTRUM", classOf[DBObject]).asScala.collect{ case x:DBObject => x.get("_id").toString}.toList

    new AsyncResult[util.List[String]](
      result.asJava
    )

  }

  @RequestMapping(path = Array("/value/{value}"), method = Array(RequestMethod.GET))
  @Async
  def listMetaDataValue(@PathVariable("value") value: String): Future[java.util.List[Any]] = {


    val aggregations = newAggregation(
      unwind("$metaData"),
      `match`(Criteria.where("metaData.name").is(value)),
      group("metaData.value")
    )

    val result:List[Any] = mongoOperations.aggregate(aggregations, "SPECTRUM", classOf[DBObject]).asScala.collect{ case x:DBObject => x.get("_id")}.toList

    new AsyncResult[util.List[Any]](
      result.asJava
    )
  }
}
