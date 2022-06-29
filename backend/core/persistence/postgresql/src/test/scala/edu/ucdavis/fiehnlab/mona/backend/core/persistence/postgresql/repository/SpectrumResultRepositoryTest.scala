package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, ContextConfiguration, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.TestConfig
import org.springframework.beans.factory.annotation.Autowired
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import org.mockito.internal.matchers.GreaterThan
import org.springframework.context.annotation.Import

import java.io.InputStreamReader

@SpringBootTest
@ActiveProfiles(Array("test"))
class SpectrumResultRepositoryTest extends AnyWordSpec with Matchers with LazyLogging {

  @Autowired
  val spectrumResultsRepo: SpectrumResultRepository = null

  @Autowired
  val mapper: ObjectMapper = null

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  new TestContextManager(this.getClass).prepareTestInstance(this)


  "Postgres Queries" should {
    s"empty database" must {
      "with deleteAll" in {
        spectrumResultsRepo.deleteAll()
        assert(spectrumResultsRepo.count() == 0)
      }
    }

    s"be able to load data" in {
      assert(spectrumResultsRepo.count() == 0)
      exampleRecords.foreach { spectrum =>
        val serialized = mapper.writeValueAsString(spectrum)
        spectrumResultsRepo.save(new SpectrumResult(spectrum.id, serialized))
      }
      assert(spectrumResultsRepo.count() == 59)
    }

    s"queries" must {
      "by monaId" in {
        logger.info(s"${spectrumResultsRepo.findByMonaId("EMBL-MCF_spec57890").getContent.length}")
        assert(spectrumResultsRepo.findByMonaId("EMBL-MCF_spec57890").getContent.length > 2)
      }

      "remove by monaId" in {
        spectrumResultsRepo.deleteByMonaId("3695602")
        spectrumResultsRepo.existsByMonaId("3695602") should be (false)
      }
    }
    s"exists" must {
      "by monaId" in {
        spectrumResultsRepo.existsByMonaId("EMBL-MCF_spec57890") should be(true)
      }
    }

    s"extras" must {
      "can count" in {
        spectrumResultsRepo.count() should be(58)
      }
    }
  }
}
