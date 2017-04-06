package edu.ucdavis.fiehnlab.mona.backend.core.persistence.service.counter

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Sequence
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.SequenceMongoRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.{Criteria, Query, Update}
import org.springframework.data.mongodb.core.{FindAndModifyOptions, MongoOperations}
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 11/22/16.
  */
@Service
class SequenceService {

  @Autowired
  private val mongoOperations: MongoOperations = null

  @Autowired
  private val sequenceRepository: SequenceMongoRepository = null


  /**
    * Gets the next value of the sequence and update the database entry.
    * Creates a new counter object for the given sequenceName if one
    * does not exist.
    * @param sequenceName
    * @return
    */
  def getNextSequenceValue(sequenceName: String): Sequence = {
    // Create new counter if one does not already exist
    if (sequenceRepository.findOne(sequenceName) == null) {
      sequenceRepository.save(Sequence(sequenceName, 0))
    }

    mongoOperations.findAndModify(
      Query.query(Criteria.where("_id").is(sequenceName)),
      new Update().inc("value", 1),
      FindAndModifyOptions.options().returnNew(true),
      classOf[Sequence])
  }

  /**
    * Generate the next unique MoNA ID
    * @return
    */
  def getNextMoNAID: String = "MoNA%06d".format(getNextSequenceValue("spectrumID").value)

  /**
    * Generate the next unique news ID
    * @return
    */
  def getNextNewsID: String = getNextSequenceValue("newsID").value.toString
}