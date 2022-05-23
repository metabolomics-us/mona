package edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.QueryExport
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.Downloader
import org.junit.runner.RunWith
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{TestContextManager, TestPropertySource}


/**
  * Created by sajjan on 6/9/16.
  */
@RunWith(classOf[SpringRunner])
@TestPropertySource(locations = Array("classpath:application.properties"))
@SpringBootTest(classes = Array(classOf[Downloader]))
class QueryExportMongoRepositoryTest extends AnyWordSpec {

  @Autowired
  val queryExportMongoRepository: QueryExportMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "QueryExportMongoRepositoryTest" should {
    "be able to save and retrieve a QueryExport object" in {
      queryExportMongoRepository.deleteAll()
      queryExportMongoRepository.save(QueryExport("test", "test", "", "json", null, null, 0, 0, null, null))

      assert(queryExportMongoRepository.count() == 1)
      assert(queryExportMongoRepository.findById("test") != null)
    }
  }
}
