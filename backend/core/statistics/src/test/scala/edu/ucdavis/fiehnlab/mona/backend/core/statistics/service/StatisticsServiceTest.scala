package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

import java.io.InputStreamReader
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SpectrumRepository, StatisticsGlobalRepository}
import org.scalatest.wordspec.AnyWordSpec
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsGlobal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by sajjan on 8/4/16.
 * */
@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class StatisticsServiceTest extends AnyWordSpec with LazyLogging {

  @Autowired
  val spectrumResultsRepo: SpectrumRepository = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  @Autowired
  private val globalStatisticsRepository: StatisticsGlobalRepository = null

  @Autowired
  val statisticsService: StatisticsService = null


  val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")), new TypeReference[Array[Spectrum]] {})

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Statistics Service" should {

    "load data" in {
      globalStatisticsRepository.deleteAll()
      spectrumResultsRepo.deleteAll()

      exampleRecords.foreach { spectrum =>
        spectrumResultsRepo.save(spectrum)
      }
      assert(spectrumResultsRepo.count() == 50)
    }

    "perform aggregation counts" in {
      val count: Long = globalStatisticsRepository.count()

      statisticsService.updateStatistics()
      val result: StatisticsGlobal = statisticsService.getGlobalStatistics

      assert(globalStatisticsRepository.count() == count + 1)

      assert(result.getSpectrumCount == 50)
      assert(result.getCompoundCount == 21)
      assert(result.getMetaDataCount == 573)
      assert(result.getMetaDataValueCount == 5154)
      assert(result.getTagCount == 2)
      assert(result.getTagValueCount == 100)
      assert(result.getSubmitterCount == 4)
    }
  }
}
