package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum

/**
  * a generic approach to connect to a REST server and execute operations against it
  */
abstract class GenericRestClient[T] (server:String) {

  /**
    * returns the count of the database content, which can be narrowed down by an optional quyery
    * @param query
    * @return
    */
  def count(query:Option[String] = None) : Long

  def add(spectrum:T) : Option[T]

  def update(spectrum: T) : Option[T]

  def delete(spectrum: T)

  def query(query:String, page:Option[Int] = None, pageSize:Option[Int] = None) : List[T]

}
