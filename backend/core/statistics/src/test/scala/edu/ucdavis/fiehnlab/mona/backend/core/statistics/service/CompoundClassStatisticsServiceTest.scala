package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
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
        spectrumResultsRepo.save(new SpectrumResult(spectrum.getId, spectrum))
      }
      assert(spectrumResultsRepo.count() == 50)
    }

    "perform aggregation" in {
      compoundClassStatisticsService.updateCompoundClassStatistics()
    }
  }
}
