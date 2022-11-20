package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{JSONDomainWriter, MonaMapper}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import org.springframework.beans.factory.annotation.Autowired

import java.io.{InputStreamReader, StringWriter}

@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SpectrumRepositoryTest extends AnyWordSpec with Matchers with LazyLogging {

  @Autowired
  val spectrumResultsRepo: SpectrumRepository = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  val writer = new JSONDomainWriter

  val out: StringWriter = new StringWriter()

  val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")), new TypeReference[Array[Spectrum]] {})

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
        spectrumResultsRepo.save(spectrum)
      }
      assert(spectrumResultsRepo.count() == 59)
    }

    s"queries" must {
      "by monaId" in {
        logger.info(s"${spectrumResultsRepo.findById("EMBL-MCF_spec57890").get().getId.length > 2}")
        assert(spectrumResultsRepo.findById("EMBL-MCF_spec57890").get().getId.length > 2)
      }

      "remove by monaId" in {
        spectrumResultsRepo.deleteById("3695602")
        spectrumResultsRepo.existsById("3695602") should be (false)
      }
    }
    s"exists" must {
      "by monaId" in {
        spectrumResultsRepo.existsById("EMBL-MCF_spec57890") should be(true)
      }
    }

    s"extras" must {
      "can count" in {
        spectrumResultsRepo.count() should be(58)
      }
    }
  }
}
