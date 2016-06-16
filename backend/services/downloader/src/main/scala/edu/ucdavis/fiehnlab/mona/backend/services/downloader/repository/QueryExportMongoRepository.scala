package edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.QueryExport
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 5/26/16.
  */
@Repository("queryExportMongoRepository")
trait QueryExportMongoRepository extends PagingAndSortingRepository[QueryExport, String] {

}