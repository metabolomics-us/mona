package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.io.InputStreamReader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.TagStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.TagStatistics
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}

/**
  * Created by sajjan on 8/4/16.
  */
@RunWith(classOf[SpringRunner])
@ContextConfiguration(classes = Array(classOf[MongoConfig], classOf[TestConfig]))
@TestPropertySource(locations = Array("classpath:application.properties"))
class TagStatisticsServiceTest extends WordSpec with LazyLogging {

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  @Qualifier("tagStatisticsMongoRepository")
  val tagStatisticsRepository: TagStatisticsMongoRepository = null

  @Autowired
  val tagStatisticsService: TagStatisticsService = null


  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Tag Statistics Service" should {

    "load data monaRecords.json" in {
      val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

      spectrumMongoRepository.deleteAll()
      exampleRecords.foreach(spectrumMongoRepository.save(_))
      assert(spectrumMongoRepository.count() == 58)
    }

    "perform tag aggregation on old MoNA records" in {
      val result: Array[TagStatistics] = tagStatisticsService.tagAggregation().sortBy(_.text)
      assert(result.length == 3)

      assert(result.map(_.text) sameElements Array("LCMS", "massbank", "noisy spectra"))
      assert(result.map(_.count) sameElements Array(58, 58, 3))
      assert(result.map(_.ruleBased) sameElements Array(false, false, false))
    }

    "persist tag statistics" in {
      tagStatisticsRepository.deleteAll()
      tagStatisticsService.updateTagStatistics()
      assert(tagStatisticsRepository.count() == 3)
    }

    "load data curatedRecords.json" in {
      val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))

      spectrumMongoRepository.deleteAll()
      exampleRecords.foreach(spectrumMongoRepository.save(_))
      assert(spectrumMongoRepository.count() == 50)
    }

    "perform tag aggregation on curated records" in {
      val result: Array[TagStatistics] = tagStatisticsService.tagAggregation().sortBy(_.text)
      assert(result.length == 2)

      assert(result.map(_.text) sameElements Array("LC-MS", "massbank"))
      assert(result.map(_.count) sameElements Array(50, 50))
      assert(result.map(_.ruleBased) sameElements Array(true, false))
    }

    "perform library tag aggregation on curated records" in {
      val result: Array[TagStatistics] = tagStatisticsService.libraryTagsAggregation()

      assert(result.length == 1)
      assert(result.head.text == "massbank")
      assert(!result.head.ruleBased)
      assert(result.head.count == 58)
      assert(result.head.category == "library")
    }
  }
}
