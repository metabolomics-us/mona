package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{ MonaMapper}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.Compound

import scala.jdk.StreamConverters.StreamHasToScala
import org.springframework.transaction.support.TransactionTemplate

import java.io.InputStreamReader

@SpringBootTest
@ActiveProfiles(Array("test"))
class CompoundRepositoryTest extends AnyWordSpec with Matchers with LazyLogging{
  @Autowired
  val compoundRepository: CompoundRepository = null

  @Autowired
  val spectrumResultsRepository: SpectrumResultRepository = null

  @Autowired
  val t: TransactionTemplate = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")), new TypeReference[Array[Spectrum]] {})

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Compound Repository" should {
    s"empty database" must {
      "with deleteAll" in {
        spectrumResultsRepository.deleteAll()
        assert(spectrumResultsRepository.count() == 0)
      }
    }

    s"load data" in {
      assert(spectrumResultsRepository.count() == 0)
      exampleRecords.foreach { spectrum =>
        //val serialized = mapper.writeValueAsString(spectrum)
        spectrumResultsRepository.save(new SpectrumResult(spectrum.getId, spectrum))
      }
      assert(spectrumResultsRepository.count() == 59)
    }

    s"check the metadata" in {
      val findIt = compoundRepository.findByMonaId("3477764")
      logger.info(s"${findIt.get(0).getMetadata.get(3).getName}")
      logger.info(s"${findIt.get(0).getMetadata.get(4).getValue}")
      logger.info(s"${findIt.get(0).getMetadata.get(2).getCategory}")
    }


    s"attempt to stream data" in {
      val c = t.execute {
         x => compoundRepository.streamAllBy().toScala(Iterator).next()
      }
      logger.info(s"${c.getMonaId}")
      c shouldBe an[Compound]
    }
  }


}
