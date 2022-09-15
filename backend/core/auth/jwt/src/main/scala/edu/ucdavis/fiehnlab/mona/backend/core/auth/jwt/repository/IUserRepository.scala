package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import javax.transaction.Transactional

@Repository
trait IUserRepository extends JpaRepository[Users, String] {

  /**
   * finds a given user by its username
   *
   * @param username
   * @return
   */
  def findByEmailAddress(emailAddress: String): Users

  @Transactional
  def deleteByEmailAddress(emailAddress: String): Unit

  def existsByEmailAddress(emailAddress: String): Boolean
}
