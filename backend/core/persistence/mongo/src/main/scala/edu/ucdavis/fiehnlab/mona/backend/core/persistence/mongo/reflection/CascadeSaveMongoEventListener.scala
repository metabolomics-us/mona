package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.reflection

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.mapping.event.{AbstractMongoEventListener, BeforeConvertEvent}
import org.springframework.util.ReflectionUtils

/**
  * Created by wohlgemuth on 3/7/16.
  */
class CascadeSaveMongoEventListener extends AbstractMongoEventListener[Any] with LazyLogging {

  @Autowired
  val mongoOperations: MongoOperations = null

  override def onBeforeConvert(event: BeforeConvertEvent[Any]): Unit = {
    ReflectionUtils.doWithFields(event.getSource.getClass, new CascadeCallback(event.getSource, mongoOperations))
  }
}
