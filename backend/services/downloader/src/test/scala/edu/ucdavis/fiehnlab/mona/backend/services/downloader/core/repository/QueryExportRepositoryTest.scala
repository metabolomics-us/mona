package edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.QueryExport
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
class QueryExportRepositoryTest extends AnyWordSpec {

  @Autowired
  val queryExportMongoRepository: QueryExportRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "QueryExportMongoRepositoryTest" should {
    "be able to save and retrieve a QueryExport object" in {
      queryExportMongoRepository.deleteAll()
      queryExportMongoRepository.save(new QueryExport("test", "test", "", "json", null, null, 0, 0, null, null))

      assert(queryExportMongoRepository.count() == 1)
      assert(queryExportMongoRepository.findById("test") != null)
    }
  }
}
