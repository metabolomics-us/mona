package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.reflection

import java.lang.reflect.Field

import org.springframework.data.annotation.Id
import org.springframework.util.ReflectionUtils

/**
  * Created by wohlgemuth on 3/7/16.
  */
class FieldCallBack extends ReflectionUtils.FieldCallback{

  private var idFound:Boolean = false

  override def doWith(field: Field): Unit = {
    ReflectionUtils.makeAccessible(field)

    if(field.isAnnotationPresent(classOf[Id])){
      idFound = true
    }
  }

  /**
    * was the id found
    * @return
    */
  def isIdFound : Boolean = {
    idFound
  }
}
