package edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.Downloader
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.PredefinedQuery
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{TestContextManager, TestPropertySource}


/**
  * Created by sajjan on 6/9/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@TestPropertySource(locations=Array("classpath:application.properties"))
@SpringApplicationConfiguration(classes = Array(classOf[Downloader]))
class PredefinedQueryMongoRepositoryTest extends WordSpec {

  @Autowired
  val predefinedQueryMongoRepository: PredefinedQueryMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "PredefinedQueryMongoRepositoryTest" should {

    "be able to save and retrive a PredefinedQuery object" in {
      predefinedQueryMongoRepository.deleteAll()
      predefinedQueryMongoRepository.save(PredefinedQuery("test", "", "", 0, null, null))

      assert(predefinedQueryMongoRepository.count() == 1)
      assert(predefinedQueryMongoRepository.findOne("test") != null)
    }
  }
}
