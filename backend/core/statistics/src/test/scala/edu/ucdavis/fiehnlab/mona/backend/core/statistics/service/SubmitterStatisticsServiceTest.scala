package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.io.InputStreamReader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.SubmitterStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.SubmitterStatistics
import org.junit.runner.RunWith
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest

/**
  * Created by sajjan on 3/9/17.
  */
@RunWith(classOf[SpringRunner])
@DataMongoTest
@ContextConfiguration(classes = Array(classOf[MongoConfig], classOf[TestConfig]))
@TestPropertySource(locations = Array("classpath:application.properties"))
class SubmitterStatisticsServiceTest extends AnyWordSpec with LazyLogging {

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  @Qualifier("submitterStatisticsMongoRepository")
  val submitterStatisticsRepository: SubmitterStatisticsMongoRepository = null

  @Autowired
  val submitterStatisticsService: SubmitterStatisticsService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Submitter Statistics Service" should {

    "load data monaRecords.json" in {
      val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

      spectrumMongoRepository.deleteAll()
      exampleRecords.foreach(spectrumMongoRepository.save(_))
      assert(spectrumMongoRepository.count() == 59)
    }

    "load data curatedRecords.json" in {
      val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))

      exampleRecords.foreach(spectrumMongoRepository.save(_))
      assert(spectrumMongoRepository.count() == 109)
    }

    "perform submitter aggregation on old MoNA records" in {
      val result: Array[SubmitterStatistics] = submitterStatisticsService.submitterAggregation()

      assert(result.length == 6)

      //assert(Math.abs(result.head.score - 0.588) < 1.0e-3)
      assert(result.head.count == 1)

      //assert(result.last.score < 1.0e-3)
      assert(result.last.count == 1)
    }

    "persist submitter statistics" in {
      submitterStatisticsRepository.deleteAll()
      submitterStatisticsService.updateSubmitterStatistics()
      assert(submitterStatisticsRepository.count() == 6)
    }
  }
}
