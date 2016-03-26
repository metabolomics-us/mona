package edu.ucdavis.fiehnlab.mona.backend.core.auth.repository

import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * allows us to work with user objects
  */
@Repository
trait UserRepository  extends PagingAndSortingRepository[User,String] {

  /**
    * finds a given user by it's username
    *
    * @param username
    * @return
    */
  def findByUsername(username:String) : User
}
