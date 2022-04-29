package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.{Repository}

@Repository
trait UserRepository extends PagingAndSortingRepository[User, String] with LazyLogging {

  abstract override def save[S <: User](s: S): S = {
    if (s.password.matches("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}")) {
      super.save(s)
    } else {
      val hashedPassword = new BCryptPasswordEncoder().encode(s.password)
      super.save(s.copy(password = hashedPassword).asInstanceOf[S])
    }
  }

  /**
    * Finds a given user by its username
    *
    * @param username
    * @return
    */
  def findByUsername(username: String): User
}
