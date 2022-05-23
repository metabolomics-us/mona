package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository

import java.lang.Iterable
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable, Sort}
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.{Component, Repository}

import java.util.Optional

/**
 * allows us to work with user objects
 */
@Repository
trait IUserRepository extends PagingAndSortingRepository[User, String] {

  /**
   * finds a given user by its username
   *
   * @param username
   * @return
   */
  def findByUsername(username: String): User
}

@Component
class UserRepository extends LazyLogging with PagingAndSortingRepository[User, String]{

  @Autowired
  val userRepository: IUserRepository = null

  def save[S <: User](s: S): S = {
    if (s.password.matches("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}")) {
      userRepository.save(s)
    } else {
      val hashedPassword = new BCryptPasswordEncoder().encode(s.password)
      userRepository.save(s.copy(password = hashedPassword).asInstanceOf[S])
    }
  }

  /**
   * Finds a given user by its username
   *
   * @param username
   * @return
   */
  def findByUsername(username: String): User = userRepository.findByUsername(username)

  override def findAll(sort: Sort): Iterable[User] = userRepository.findAll(sort)

  override def findAll(pageable: Pageable): Page[User] = userRepository.findAll(pageable)

  override def saveAll[S <: User](entities: Iterable[S]): Iterable[S] = userRepository.saveAll(entities)

  override def findById(id: String): Optional[User] = userRepository.findById(id)

  override def existsById(id: String): Boolean = userRepository.existsById(id)

  override def findAll(): Iterable[User] = userRepository.findAll()

  override def findAllById(ids: Iterable[String]): Iterable[User] = userRepository.findAllById(ids)

  override def count(): Long = userRepository.count()

  override def deleteById(id: String): Unit = userRepository.deleteById(id)

  override def delete(entity: User): Unit = userRepository.delete(entity)

  override def deleteAll(entities: Iterable[_ <: User]): Unit = userRepository.deleteAll(entities)

  override def deleteAll(): Unit = userRepository.deleteAll()
}
