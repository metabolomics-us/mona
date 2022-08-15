package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

import java.io.InputStreamReader
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SpectrumResultRepository, StatisticsGlobalRepository}
import org.scalatest.wordspec.AnyWordSpec
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics.StatisticsGlobal
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by sajjan on 8/4/16.
 * */
@SpringBootTest
@ActiveProfiles(Array("test"))
class StatisticsServiceTest extends AnyWordSpec with LazyLogging {

  @Autowired
  val spectrumResultsRepo: SpectrumResultRepository = null

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
        spectrumResultsRepo.save(new SpectrumResult(spectrum.getId, spectrum))
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
      assert(result.getMetaDataCount == 574)
      assert(result.getMetaDataValueCount == 5254)
      assert(result.getTagCount == 2)
      assert(result.getTagValueCount == 100)
      assert(result.getSubmitterCount == 4)
    }
  }
}
