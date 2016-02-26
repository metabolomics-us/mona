package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import java.io.File

import com.mongodb.Mongo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types._
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.RepositoryConfiguration
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterEach, FunSuite}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.{TestContextManager, ContextHierarchy, ContextConfiguration}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlgemuth on 2/25/16.
  */

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RepositoryConfiguration],classOf[TestMongoDBConfig]))
class RepositoryServiceTest extends FunSuite with BeforeAndAfterEach {

  @Autowired
  val spectrumRepositoryService:RepositoryService[Spectrum] = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  val exampleRecord = JSONDomainReader.create[Spectrum].read(new File("src/test/resources/monaRecord.json")).head

  override def beforeEach() {

  }

  override def afterEach() {

  }

  test("testGet") {

  }

  test("testDelete") {

  }

  test("testSave") {
    spectrumRepositoryService.save(exampleRecord)
  }

  test("testQuery") {

  }

}
