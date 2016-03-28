package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.metadata

import java.util
import java.util.concurrent.Future

import com.mongodb.DBObject
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.WrappedString
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation._
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.scheduling.annotation.{Async, AsyncResult}
import org.springframework.web.bind.annotation._

import scala.collection.JavaConverters._

/**
  * Created by wohlg_000 on 3/7/2016.
  */
@CrossOrigin
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

  @RequestMapping(path = Array("/values"), method = Array(RequestMethod.POST))
  @Async
  def listMetaDataValue(@RequestBody metaDataName: WrappedString): Future[java.util.List[Any]] = {

    val aggregations = newAggregation(
      unwind("$metaData"),
      `match`(Criteria.where("metaData.name").is(metaDataName.string)),
      group("metaData.value")
    )

    val result:List[Any] = mongoOperations.aggregate(aggregations, "SPECTRUM", classOf[DBObject]).asScala.collect{ case x:DBObject => x.get("_id")}.toList

    new AsyncResult[util.List[Any]](
      result.asJava
    )
  }
}
