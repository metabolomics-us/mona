package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Submitter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.elasticsearch.{ElasticsearchAutoConfiguration, ElasticsearchDataAutoConfiguration}
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/23/16.
  */
@RunWith(classOf[SpringRunner])
@ContextConfiguration(classes = Array(classOf[Config]))
@TestPropertySource(locations = Array("classpath:application.properties"))
class ISubmitterMongoRepositoryTest extends WordSpec {

  @Autowired
  val submitterMongoRepository: ISubmitterMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "ISubmitterMongoRepositoryTest" should {

    "delete all data " in {
      submitterMongoRepository.deleteAll()
      assert(submitterMongoRepository.count() == 0)
    }

    "add submitter" in {
      submitterMongoRepository.save(Submitter(null, "test", "Test", "User", "UC Davis"))
      assert(submitterMongoRepository.count() == 1)
    }

    "findByFirstName" in {
      assert(submitterMongoRepository.findByFirstName("Test").asScala.size == 1)
    }

    "findByEmail" in {
      assert(submitterMongoRepository.findByEmailAddress("test").firstName == "Test")
      assert(submitterMongoRepository.findByEmailAddress("test").lastName == "User")
      assert(submitterMongoRepository.findByEmailAddress("test").institution == "UC Davis")
    }
  }
}

@SpringBootApplication(exclude = Array(classOf[ElasticsearchAutoConfiguration], classOf[ElasticsearchDataAutoConfiguration]))
@Import(Array(classOf[MongoConfig]))
class Config