package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.Users
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
trait IUserRepository extends PagingAndSortingRepository[Users, String] {

  /**
   * finds a given user by its username
   *
   * @param username
   * @return
   */
  def findByEmailAddress(emailAddress: String): Users
}
