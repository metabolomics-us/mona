package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.fasterxml.jackson.databind.ObjectMapper

import java.io.InputStreamReader
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SpectrumResultRepository, StatisticsGlobalRepository}
import org.scalatest.wordspec.AnyWordSpec
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.statistics.{StatisticsGlobal}
import org.springframework.beans.factory.annotation.{Autowired}
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
  val mapper: ObjectMapper = null

  @Autowired
  private val globalStatisticsRepository: StatisticsGlobalRepository = null

  @Autowired
  val statisticsService: StatisticsService = null


  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Statistics Service" should {

    "load data" in {
      globalStatisticsRepository.deleteAll()
      spectrumResultsRepo.deleteAll()

      exampleRecords.foreach { spectrum =>
        val serialized = mapper.writeValueAsString(spectrum)
        spectrumResultsRepo.save(new SpectrumResult(spectrum.id, serialized))
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
