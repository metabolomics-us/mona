package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.Sequence
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SequenceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SequenceService {
  @Autowired
  private val sequenceRepository: SequenceRepository = null


  /**
   * Gets the next value of the sequence and update the database entry.
   * Creates a new counter object for the given sequenceName if one
   * does not exist.
   *
   * @param sequenceName
   * @return
   */
  def getNextSequenceValue(sequenceName: String): Sequence = {
    // Create new counter if one does not already exist
    val curValue: Sequence = sequenceRepository.findById(sequenceName).orElse(null)
    if (curValue == null) {
      sequenceRepository.save(new Sequence(sequenceName, 0))
    }

    //Should not be null
    val newValue: Sequence = sequenceRepository.findById(sequenceName).get()
    newValue.setValue(newValue.getValue + 1)

    sequenceRepository.saveAndFlush(newValue)
    newValue

  }

  /**
   * Generate the next unique MoNA ID
   *
   * @return
   */
  def getNextMoNAID: String = "MoNA_%06d".format(getNextSequenceValue("spectrumID").getValue)

  /**
   * Generate the next unique news ID
   *
   * @return
   */
  def getNextNewsID: String = getNextSequenceValue("newsID").getValue.toString
}
