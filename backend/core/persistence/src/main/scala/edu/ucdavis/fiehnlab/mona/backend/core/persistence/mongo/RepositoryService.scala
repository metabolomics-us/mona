package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import scala.reflect._

import scala.reflect.ClassTag

/**
  * generic service to interact with the mongo database, however we configure it
  */
@Service
class RepositoryServiceImpl[T: ClassTag] extends RepositoryService[T]{

  @Autowired
  val mongoTemplate: MongoTemplate = null

  /**
    * saves a new entry or updates an existing once, in case of an existing id symbol
    *
    * @param entry
    */
  def save(entry: T) = {
    mongoTemplate.save(entry)
  }

  /**
    * deletes an entry
    *
    * @param entry
    * @return
    */
  def delete(entry: T) = {
    mongoTemplate.remove(entry)
  }

  /**
    * returns the object with the given key
    *
    * @param key
    * @return
    */
  def get(key: String): T = {
    mongoTemplate.findById(key, classTag[T].runtimeClass).asInstanceOf[T]
  }

  /**
    * provide a query to receive a list of the provided  objects
    *
    * @param query
    * @return
    */
  def query(query: Query): java.util.List[T] = {
    mongoTemplate.find(query, classTag[T].runtimeClass).asInstanceOf[java.util.List[T]]
  }
}

/**
  * general definition of a repository service
  * @tparam T
  */
trait RepositoryService[T]{

  /**
    * saves a new entry or updates an existing once, in case of an existing id symbol
    *
    * @param entry
    */
  def save(entry: T)
  /**
    * deletes an entry
    *
    * @param entry
    * @return
    */
  def delete(entry: T)
  /**
    * returns the object with the given key
    *
    * @param key
    * @return
    */
  def get(key: String): T
  /**
    * provide a query to receive a list of the provided  objects
    *
    * @param query
    * @return
    */
  def query(query: Query): java.util.List[T]
}