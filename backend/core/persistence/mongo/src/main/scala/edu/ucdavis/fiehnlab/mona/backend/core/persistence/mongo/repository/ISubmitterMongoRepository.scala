package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Submitter
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

/**
  * Created by sajjan on 3/9/16.
  */
@Repository("submitterMongoRepository")
trait ISubmitterMongoRepository extends PagingAndSortingRepository[Submitter, String] {

  /**
    * returns the submitter with this email address
    * @param email
    * @return
    */
  def findByEmailAddress(email:String) : Submitter

  /**
    * returns all submitters by there first name
    * @param firstName
    * @return
    */
  def findByFirstName(firstName:String) : java.lang.Iterable[Submitter]

  /**
    * returns the submitter by it's id property
    * @param id
    * @return
    */
  def findById(id:String) : Submitter
}