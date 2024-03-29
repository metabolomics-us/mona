package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumRepository
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by sajjan on 8/4/16.
* */

@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class CompoundClassStatisticsServiceTest extends AnyWordSpec {

  @Autowired
  val spectrumResultsRepo: SpectrumRepository = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  @Autowired
  val compoundClassStatisticsService: CompoundClassStatisticsService = null

  val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")), new TypeReference[Array[Spectrum]] {})

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Compound Class Statistics Service" should {

    "load data" in {
      spectrumResultsRepo.deleteAll()
      exampleRecords.foreach { spectrum =>
        spectrumResultsRepo.save(spectrum)
      }
      assert(spectrumResultsRepo.count() == 50)
    }

    "perform aggregation" in {
      compoundClassStatisticsService.updateCompoundClassStatistics()
    }
  }
}
