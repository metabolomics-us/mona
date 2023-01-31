package edu.ucdavis.fiehnlab.mona.backend.curation.writer

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.config.JWTAuthenticationConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientTestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.service.SpectrumPersistenceService
import edu.ucdavis.fiehnlab.mona.backend.curation.TestConfig
import org.hibernate.Hibernate
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

import scala.jdk.CollectionConverters._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Created by wohlg on 3/11/2016.
  */
@SpringBootTest(classes = Array(classOf[RestClientTestConfig], classOf[TestConfig], classOf[JWTAuthenticationConfig]), webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class RestRepositoryWriterTest extends AnyWordSpec with Eventually with BeforeAndAfterEach{

  @Autowired
  val monaSpectrumRestClient: MonaSpectrumRestClient = null

  @Autowired
  val writer: RestRepositoryWriter = null

  @Autowired
  val spectrumPersistenceService: SpectrumPersistenceService = null

  @Autowired
  private val transactionManager: PlatformTransactionManager = null

  private var transactionTemplate: TransactionTemplate = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  protected override def beforeEach() = (
    transactionTemplate = new TransactionTemplate(transactionManager)
    )

  protected override def afterEach() = (
    transactionTemplate = null
    )

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "a writer " when {
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "given a list of spectra" should {
      "clear data first" in {
        transactionTemplate.execute{ x =>
          spectrumPersistenceService.deleteAll()
          Hibernate.initialize()
          x
        }

        eventually(timeout(100 seconds)) {
          assert(spectrumPersistenceService.count() == 0)
        }
      }

      "upload them to the server" in {
        writer.write(exampleRecords.toList.asJava)
        eventually(timeout(10 seconds)) {
          assert(monaSpectrumRestClient.count() == exampleRecords.length)
        }
      }
    }
  }
}
