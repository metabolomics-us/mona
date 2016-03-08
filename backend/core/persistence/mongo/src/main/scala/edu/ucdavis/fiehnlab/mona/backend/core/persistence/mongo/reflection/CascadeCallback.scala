package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.reflection

import java.lang.reflect.Field

import com.typesafe.scalalogging.LazyLogging
import org.springframework.data.mongodb.core.mapping.DBRef
import edu.ucdavis.fiehnlab.mona.backend.core.domain.annotation.CascadeSave
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.util.ReflectionUtils;

/**
  * Created by wohlgemuth on 3/7/16.
  */
class CascadeCallback(val source: Any, val mongoOperations: MongoOperations) extends ReflectionUtils.FieldCallback with LazyLogging {
  override def doWith(field: Field): Unit = {
    ReflectionUtils.makeAccessible(field)

    if (field.isAnnotationPresent(classOf[DBRef]) && field.isAnnotationPresent(classOf[CascadeSave])) {

      val fieldValue: Any = field.get(source)

      if (fieldValue != null) {
        val callback = new FieldCallBack

        logger.debug(s"updating references for field: ${fieldValue}")

        ReflectionUtils.doWithFields(fieldValue.getClass, callback)

        mongoOperations.save(fieldValue)
      }
    }
  }
}
