package edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.Downloader
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.QueryExport
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.{TestContextManager, TestPropertySource}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner


/**
  * Created by sajjan on 6/9/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@TestPropertySource(locations=Array("classpath:application.properties"))
@SpringApplicationConfiguration(classes = Array(classOf[Downloader]))
class QueryExportMongoRepositoryTest extends WordSpec {

  @Autowired
  val queryExportMongoRepository: QueryExportMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "QueryExportMongoRepositoryTest" should {

    "be able to save and retrive a QueryExport object" in {
      queryExportMongoRepository.deleteAll()
      queryExportMongoRepository.save(QueryExport("test", "test", "", "json", null, null, 0, 0, null, null))

      assert(queryExportMongoRepository.count() == 1)
      assert(queryExportMongoRepository.findOne("test") != null)
    }
  }
}
