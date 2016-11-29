package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Sequence
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 11/22/16.
  */
@Repository("sequenceMongoRepository")
trait SequenceMongoRepository extends CrudRepository[Sequence, String]