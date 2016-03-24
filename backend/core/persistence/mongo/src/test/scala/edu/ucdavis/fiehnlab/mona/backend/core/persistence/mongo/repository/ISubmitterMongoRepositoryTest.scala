package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Submitter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.{ContextConfiguration, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import scala.collection.JavaConverters._
/**
  * Created by wohlgemuth on 3/23/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[EmbeddedMongoDBConfiguration]))
class ISubmitterMongoRepositoryTest extends WordSpec {

  @Autowired
  val submitterMongoRepository:ISubmitterMongoRepository = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "ISubmitterMongoRepositoryTest" should {

    "delete all data " in {
      submitterMongoRepository.deleteAll()
      assert(submitterMongoRepository.count() == 0)
    }
    "add submitter" in {
      submitterMongoRepository.save(Submitter("wohlgemuth@ucdavis.edu","gert","uc davis", "wohlgemuth"))
      assert(submitterMongoRepository.count() == 1)
    }
    "findByFirstName" in {
      assert(submitterMongoRepository.findByFirstName("gert").asScala.size == 1)
    }

    "findByEmail" in {
      assert(submitterMongoRepository.findByEmailAddress("wohlgemuth@ucdavis.edu").firstName == "gert")
    }

  }
}
