package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

import java.io.InputStreamReader
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SpectrumRepository, StatisticsSubmitterRepository}
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by sajjan on 3/9/17.
 * */

@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SubmitterStatisticsServiceTest extends AnyWordSpec with LazyLogging {
  @Autowired
  val spectrumResultsRepo: SpectrumRepository = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  @Autowired
  val submitterStatisticsService: SubmitterStatisticsService = null

  @Autowired
  val statisticsSubmitterRepository: StatisticsSubmitterRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Submitter Statistics Service" should {

    "load data monaRecords.json" in {
      val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")), new TypeReference[Array[Spectrum]] {})

      spectrumResultsRepo.deleteAll()
      exampleRecords.foreach { spectrum =>
        spectrumResultsRepo.save(spectrum)
      }
      assert(spectrumResultsRepo.count() == 59)
    }

    "load data curatedRecords.json" in {
      val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")), new TypeReference[Array[Spectrum]] {})

      exampleRecords.foreach { spectrum =>
        spectrumResultsRepo.save(spectrum)
      }
      assert(spectrumResultsRepo.count() == 109)
    }

    "perform submitter aggregation on old MoNA records" in {
      submitterStatisticsService.updateSubmitterStatistics()

      assert(statisticsSubmitterRepository.count() == 6)

      val results = submitterStatisticsService.getSubmitterStatistics

      //assert(Math.abs(result.head.score - 0.588) < 1.0e-3)
      assert(results.head.getCount == 1)

      //assert(result.last.score < 1.0e-3)
      assert(results.last.getCount == 1)
    }
  }
}
