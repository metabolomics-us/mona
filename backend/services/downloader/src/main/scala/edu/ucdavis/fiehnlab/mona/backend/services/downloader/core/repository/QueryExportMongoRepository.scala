package edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.QueryExport
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 5/26/16.
  */
@Repository("queryExportMongoRepository")
trait QueryExportMongoRepository extends PagingAndSortingRepository[QueryExport, String]