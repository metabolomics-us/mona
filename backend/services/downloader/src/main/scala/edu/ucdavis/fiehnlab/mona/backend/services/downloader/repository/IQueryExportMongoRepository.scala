package edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.QueryExport
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 5/26/16.
  */
@Repository("queryExportMongoRepository")
trait IQueryExportMongoRepository extends PagingAndSortingRepository[QueryExport, String] {

  /**
    * returns the query export with this email address
    *
    * @param label
    * @return
    */
  def findByLabel(label: String): QueryExport

  /**
    * returns the query export by it's id property
    *
    * @param id
    * @return
    */
  def findById(id: String) : QueryExport
}