package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager

/**
  * Assigns sequential, collision free numbers for a library form upload's custom spectrum id
  * prefix (PREFIX000001, PREFIX000002, ...). Backed by a single atomic
  * "INSERT ... ON CONFLICT ... DO UPDATE ... RETURNING" statement 
  */
@Service
@Profile(Array("mona.persistence"))
class LibraryPrefixCounterService {

  @Autowired
  private val entityManager: EntityManager = null

  @Transactional
  def nextValue(prefix: String): Long = {
    val result = entityManager.createNativeQuery(
      """
        |INSERT INTO library_prefix_counter (prefix, next_value) VALUES (?1, 1)
        |ON CONFLICT (prefix) DO UPDATE SET next_value = library_prefix_counter.next_value + 1
        |RETURNING next_value
        |""".stripMargin
    ).setParameter(1, prefix).getSingleResult

    result.asInstanceOf[Number].longValue()
  }
}
