package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.fasterxml.jackson.databind.ObjectMapper

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by sajjan on 8/4/16.
* */

@SpringBootTest
@ActiveProfiles(Array("test"))
class CompoundClassStatisticsServiceTest extends AnyWordSpec {

  @Autowired
  val spectrumResultsRepo: SpectrumResultRepository = null

  @Autowired
  val mapper: ObjectMapper = null

  @Autowired
  val compoundClassStatisticsService: CompoundClassStatisticsService = null

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Compound Class Statistics Service" should {

    "load data" in {
      spectrumResultsRepo.deleteAll()
      exampleRecords.foreach { spectrum =>
        val serialized = mapper.writeValueAsString(spectrum)
        spectrumResultsRepo.save(new SpectrumResult(spectrum.id, serialized))
      }
      assert(spectrumResultsRepo.count() == 50)
    }

    "perform aggregation" in {
      compoundClassStatisticsService.updateCompoundClassStatistics()
    }
  }
}
