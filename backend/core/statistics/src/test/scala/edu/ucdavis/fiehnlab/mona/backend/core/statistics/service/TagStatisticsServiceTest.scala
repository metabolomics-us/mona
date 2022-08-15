package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

import java.io.InputStreamReader
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SpectrumResultRepository, StatisticsTagRepository}
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 8/4/16.
 * */
@SpringBootTest
@ActiveProfiles(Array("test"))
class TagStatisticsServiceTest extends AnyWordSpec with LazyLogging {

  @Autowired
  val spectrumResultsRepo: SpectrumResultRepository = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  @Autowired
  val tagStatisticsService: TagStatisticsService = null

  @Autowired
  val statisticsTagRepository: StatisticsTagRepository = null


  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Tag Statistics Service" should {

    "load data monaRecords.json" in {
      val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")), new TypeReference[Array[Spectrum]] {})

      spectrumResultsRepo.deleteAll()
      exampleRecords.foreach { spectrum =>
        spectrumResultsRepo.save(new SpectrumResult(spectrum.getId, spectrum))
      }
      assert(spectrumResultsRepo.count() == 59)
    }

    "perform tag aggregation on old MoNA records" in {
      tagStatisticsService.updateTagStatistics()
      assert(statisticsTagRepository.count() == 5)
      val result = statisticsTagRepository.findAll().asScala.sortBy(_.getText)

      assert(result.map(_.getText) sameElements Array("EMBL-MCF", "LC-MS", "LCMS", "massbank", "noisy spectra"))
      assert(result.map(_.getCount) sameElements Array(1, 1, 58, 58, 3))
      assert(result.map(_.getRuleBased) sameElements Array(false, true, false, false, false))
    }

    "load data curatedRecords.json" in {
      val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")), new TypeReference[Array[Spectrum]] {})

      spectrumResultsRepo.deleteAll()
      exampleRecords.foreach { spectrum =>
        spectrumResultsRepo.save(new SpectrumResult(spectrum.getId, spectrum))
      }
      assert(spectrumResultsRepo.count() == 50)
    }

    "perform tag aggregation on curated records" in {
      tagStatisticsService.updateTagStatistics()
      assert(statisticsTagRepository.count() == 2)
      val result = statisticsTagRepository.findAll().asScala.sortBy(_.getText)

      assert(result.map(_.getText) sameElements Array("LC-MS", "massbank"))
      assert(result.map(_.getCount) sameElements Array(50, 50))
      assert(result.map(_.getRuleBased) sameElements Array(true, false))
    }

    "perform library tag aggregation on curated records" in {
      val result = tagStatisticsService.getLibraryTagStatistics

      assert(result.size == 1)
      assert(result.head.getText == "massbank")
      assert(!result.head.getRuleBased)
      assert(result.head.getCount == 50)
      assert(result.head.getCategory == "library")
    }
  }
}
