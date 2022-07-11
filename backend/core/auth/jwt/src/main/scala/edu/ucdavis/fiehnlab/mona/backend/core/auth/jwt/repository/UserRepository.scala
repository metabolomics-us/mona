package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.Users
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.{Page, Pageable, Sort}
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.{Component}

import java.lang
import java.util.Optional

@Component
class UserRepository extends LazyLogging with PagingAndSortingRepository[Users, String]{

  @Autowired
  val userRepository: IUserRepository = null

  def save[S <: Users](s: S): S = {
    if (s.getPassword.matches("\\A\\$2a?\\$\\d\\d\\$[./0-9A-Za-z]{53}")) {
      userRepository.save(s)
    } else {
      val hashedPassword = new BCryptPasswordEncoder().encode(s.getPassword)
      s.setPassword(hashedPassword)
      userRepository.save(s)
    }
  }

  /**
   * Finds a given user by its username
   *
   * @param username
   * @return
   */
  def findByUsername(username: String): Users = userRepository.findByEmailAddress(username)

  override def findAll(sort: Sort): lang.Iterable[Users] = userRepository.findAll(sort)

  override def findAll(pageable: Pageable): Page[Users] = userRepository.findAll(pageable)

  override def saveAll[S <: Users](entities: lang.Iterable[S]): lang.Iterable[S] = userRepository.saveAll(entities)

  override def findById(id: String): Optional[Users] = userRepository.findById(id)

  override def existsById(id: String): Boolean = userRepository.existsById(id)

  override def findAll(): lang.Iterable[Users] = userRepository.findAll()

  override def findAllById(ids: lang.Iterable[String]): lang.Iterable[Users] = userRepository.findAllById(ids)

  override def count(): Long = userRepository.count()

  override def deleteById(id: String): Unit = userRepository.deleteById(id)

  override def delete(entity: Users): Unit = userRepository.delete(entity)

  override def deleteAllById(ids: lang.Iterable[_ <: String]): Unit = userRepository.deleteAllById(ids)

  override def deleteAll(entities: lang.Iterable[_ <: Users]): Unit = userRepository.deleteAll(entities)

  override def deleteAll(): Unit = userRepository.deleteAll()
}
