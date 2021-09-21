package edu.ucdavis.fiehnlab.mona.app.server.proxy.repository

import edu.ucdavis.fiehnlab.mona.app.server.proxy.logging.LogMessage
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 9/26/16.
  */
@Repository("logMessageMongoRepository")
trait LogMessageMongoRepository extends PagingAndSortingRepository[LogMessage, String]