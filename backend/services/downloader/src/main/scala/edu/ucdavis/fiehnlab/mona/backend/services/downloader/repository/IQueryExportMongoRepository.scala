package edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.QueryExport
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 5/26/16.
  */
@Repository("queryExportMongoRepository")
trait IQueryExportMongoRepository extends PagingAndSortingRepository[QueryExport, String] {

  /**
    * returns the query export with the given label
    *
    * @param label
    * @return
    */
  def findByLabel(label: String): QueryExport

  /**
    * returns the query exports with the given email address
    *
    * @param emailAddress
    * @return
    */
  def findByEmailAddress(emailAddress: String): Iterable[QueryExport]

  /**
    * returns the query exports with the given query
    *
    * @param query
    * @return
    */
  def findByQuery(query: String): Iterable[QueryExport]
}