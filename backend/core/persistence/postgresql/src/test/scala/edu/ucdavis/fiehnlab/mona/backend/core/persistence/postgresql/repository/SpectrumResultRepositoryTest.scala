package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, ContextConfiguration, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.TestConfig
import org.springframework.beans.factory.annotation.Autowired
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult

import java.io.InputStreamReader

@SpringBootTest
@ActiveProfiles(Array("test"))
class SpectrumResultRepositoryTest extends AnyWordSpec with Matchers {

  @Autowired
  val spectrumResultsRepo: SpectrumResultRepository = null

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Postgres Queries" should {
    s"Should be able to load data" in {
      exampleRecords.foreach { spectrum =>
        val size = spectrumResultsRepo.count()
        spectrumResultsRepo.save(new SpectrumResult(spectrum.id, spectrum.toString))
        val newSize = spectrumResultsRepo.count()
        assert(newSize == size + 1)
      }
    }
  }
}
