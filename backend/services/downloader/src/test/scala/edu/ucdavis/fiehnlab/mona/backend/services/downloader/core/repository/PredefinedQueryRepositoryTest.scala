package edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.PredefinedQuery
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.Downloader
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}


/**
  * Created by sajjan on 6/9/16.
 * */
@SpringBootTest(classes = Array(classOf[Downloader]))
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.downloader", "mona.persistence.init"))
class PredefinedQueryRepositoryTest extends AnyWordSpec {

  @Autowired
  val predefinedQueryMongoRepository: PredefinedQueryRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "PredefinedQueryMongoRepositoryTest" should {

    "be able to save and retrieve a PredefinedQuery object" in {
      predefinedQueryMongoRepository.deleteAll()
      predefinedQueryMongoRepository.save(new PredefinedQuery("test", "", "", 0, null, null, null))

      assert(predefinedQueryMongoRepository.count() == 1)
      assert(predefinedQueryMongoRepository.findById("test") != null)
    }
  }
}
