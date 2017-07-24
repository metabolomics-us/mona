package edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.PredefinedQuery
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.Downloader
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{TestContextManager, TestPropertySource}


/**
  * Created by sajjan on 6/9/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@TestPropertySource(locations = Array("classpath:application.properties"))
@SpringApplicationConfiguration(classes = Array(classOf[Downloader]))
class PredefinedQueryMongoRepositoryTest extends WordSpec {

  @Autowired
  val predefinedQueryMongoRepository: PredefinedQueryMongoRepository = null

  @Autowired
  val mongoOperations: MongoOperations = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "PredefinedQueryMongoRepositoryTest" should {

    "be able to save and retrieve a PredefinedQuery object" in {
      predefinedQueryMongoRepository.deleteAll()
      predefinedQueryMongoRepository.save(PredefinedQuery("test", "", "", 0, null, null))

      assert(predefinedQueryMongoRepository.count() == 1)
      assert(predefinedQueryMongoRepository.findOne("test") != null)
    }
  }
}
