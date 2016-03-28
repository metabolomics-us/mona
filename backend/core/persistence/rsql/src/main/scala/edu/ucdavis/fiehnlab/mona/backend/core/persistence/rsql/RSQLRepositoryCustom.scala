package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql

import java.util

import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.repository.NoRepositoryBean

/**
  * Created by wohlgemuth on 3/9/16.
  */

@NoRepositoryBean
trait RSQLRepositoryCustom[T,Q] {

  /**
    * @param query
    * @return
    */
  def nativeQuery(query: Q): util.List[T]

  /**
    * executes a query against the system and returns the count
    *
    * @param query
    * @return
    */
  def nativeQueryCount(query: Q): Long

  /**
    *
    * @param query
    * @return
    *
    */
  def nativeQuery(query: Q, pageable: Pageable): Page[T]


  /**
    * executes an RSQL query
    *
    * @param query
    * @return
    */
  def rsqlQuery(query: String): java.util.List[T] = nativeQuery(buildRSQLQuery(query))


  /**
    *
    * @param query
    * @return
    */
  def rsqlQuery(query: String, pageable: Pageable): Page[T] = nativeQuery(buildRSQLQuery(query), pageable)


  /**
    * executes the given query and returns it's count
    *
    * @param query
    * @return
    */
  def rsqlQueryCount(query: String): Long = nativeQueryCount(buildRSQLQuery(query))

  /**
    * converts the RSQL String for us to a Query Object
    *
    * @param query
    * @return
    */
  def buildRSQLQuery(query: String): Q

  /**
    * saves our updaes a given element
    * implementation can be slow but should not cause
    * duplicated saves
    *
    * @param value
    * @return
    */
  def saveOrUpdate(value: T): Unit
}