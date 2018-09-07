package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository

import java.lang.Iterable
import java.util.Optional

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable, Sort}
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.{Component, Repository}

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
class UserRepository extends PagingAndSortingRepository[User, String] with LazyLogging {

  @Autowired
  val userRepository: IUserRepository = null


  override def findAll(pageable: Pageable): Page[User] = userRepository.findAll(pageable)

  override def findAll(sort: Sort): Iterable[User] = userRepository.findAll(sort)

  override def deleteById(id: String): Unit = userRepository.deleteById(id)

  def findOne(id: String): User = findById(id).orElse(null)

  override def findById(id: String): Optional[User] = userRepository.findById(id)

  override def findAll(): Iterable[User] = userRepository.findAll()

  override def deleteAll(iterable: Iterable[_ <: User]): Unit = userRepository.deleteAll(iterable)

  override def deleteAll(): Unit = userRepository.deleteAll()

  override def findAllById(iterable: Iterable[String]): Iterable[User] = userRepository.findAllById(iterable)

  override def existsById(id: String): Boolean = userRepository.existsById(id)

  override def count(): Long = userRepository.count()

  override def delete(t: User): Unit = userRepository.delete(t)

  override def saveAll[S <: User](iterable: Iterable[S]): Iterable[S] = userRepository.saveAll(iterable)

  override def save[S <: User](s: S): S = {
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
}
