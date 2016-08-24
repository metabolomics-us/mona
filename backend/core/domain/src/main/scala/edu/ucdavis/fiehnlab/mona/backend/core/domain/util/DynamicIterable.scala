package edu.ucdavis.fiehnlab.mona.backend.core.domain.util

import java.lang.Iterable

import com.typesafe.scalalogging.LazyLogging
import org.springframework.data.domain.{Page, PageRequest, Pageable}

/**
  * Created by wohlgemuth on 3/18/16.
  */
abstract class DynamicIterable[T,Q](val query:Q, val fetchSize:Int = 10, val transformer:(T) => T ) extends Iterable[T] with LazyLogging{

  def this(query:Q,fetchSize:Int) = this(query,fetchSize,{t:T => t})

  /**
    * loads more data from the server for the given query
    */
  def fetchMoreData(query:Q, pageable:Pageable) : Page[T]

  /**
    * defines our custom batch fetching iterator
    *
    * @return
    */
  override def iterator: java.util.Iterator[T] = new java.util.Iterator[T] {
    var result = fetchMoreData(query, new PageRequest(0, fetchSize))
    var it = result.iterator()
    var page: Int = 1

    override def hasNext: Boolean = {
      if (!it.hasNext) {
        logger.debug(s"fetching new set of data, page ${result.getNumber} is exhausted, fetch size is ${fetchSize}")
        result = fetchMoreData(query, new PageRequest(page, fetchSize))
        it = result.iterator()
        page = page + 1

        if (!it.hasNext) {
          logger.debug(s"all data are loaded for query: ${query}")
        }
      }

      it.hasNext
    }

    /**
      * fetches the next spectrum from the database
      *
      * @return
      */
    override def next(): T= {
      transformer(it.next())
    }
  }
}
