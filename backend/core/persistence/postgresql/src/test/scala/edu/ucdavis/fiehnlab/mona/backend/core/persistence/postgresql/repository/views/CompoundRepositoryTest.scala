package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{CompoundRepository, SpectrumRepository}

import scala.jdk.StreamConverters.StreamHasToScala
import org.springframework.transaction.support.TransactionTemplate

import java.io.InputStreamReader

@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class CompoundRepositoryTest extends AnyWordSpec with Matchers with LazyLogging{
  @Autowired
  val compoundRepository: CompoundRepository = null

  @Autowired
  val spectrumResultsRepository: SpectrumRepository = null

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
        spectrumResultsRepository.save(spectrum)
      }
      assert(spectrumResultsRepository.count() == 59)
    }


    s"attempt to stream data" in {
      val c = t.execute {
         x => compoundRepository.streamAllBy().toScala(Iterator).next()
      }
      logger.info(s"${c.getId}")
      c shouldBe an[Compound]
    }
  }


}
