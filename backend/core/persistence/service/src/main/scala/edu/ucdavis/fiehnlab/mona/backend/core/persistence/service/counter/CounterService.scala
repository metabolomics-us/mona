package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.counter

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Counter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.CounterMongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.{FindAndModifyOptions, MongoOperations}
import org.springframework.data.mongodb.core.query.{Criteria, Query, Update}
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.{Repository, Service}

/**
  * Created by sajjan on 11/22/16.
  */
@Service
class CounterService {

  @Autowired
  private val mongoOperations: MongoOperations = null

  @Autowired
  private val counterRepository: CounterMongoRepository = null


  def getNextSequence(counterName: String): Counter = {

    // Create new counter if one does not already exist
    if (counterRepository.findOne(counterName) == null) {
      counterRepository.save(Counter(counterName, 0))
    }

    println(mongoOperations.findAll(classOf[Counter]).size())

    mongoOperations.findAndModify(
      Query.query(Criteria.where("_id").is(counterName)),
      new Update().inc("count", 1),
      FindAndModifyOptions.options().returnNew(true),
      classOf[Counter])
  }
}