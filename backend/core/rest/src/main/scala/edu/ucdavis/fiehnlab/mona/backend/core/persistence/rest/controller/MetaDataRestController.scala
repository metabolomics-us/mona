package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.controller

import java.util
import java.util.concurrent.Future

import com.mongodb.DBObject
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.ISpectrumMongoRepositoryCustom
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.scheduling.annotation.{AsyncResult, Async}
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, RestController}

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
    new AsyncResult[java.util.List[String]](
      mongoOperations.getCollection("SPECTRUM").distinct("metaData.value").asInstanceOf[java.util.List[String]]
    )
  }

  @RequestMapping(path = Array("/value"), method = Array(RequestMethod.GET))
  @Async
  def listMetaDataValue : Future[java.util.List[Any]] = {

    new AsyncResult[util.List[Any]](

      mongoOperations.getCollection("SPECTRUM").distinct("metaData.value",).asInstanceOf[java.util.List[Any]]
    )
  }
}
