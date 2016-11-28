package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Counter
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 11/22/16.
  */
@Repository("counterMongoRepository")
trait CounterMongoRepository extends CrudRepository[Counter, String]