package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.io.InputStreamReader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.GlobalStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.GlobalStatistics
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
class StatisticsServiceTest extends WordSpec with LazyLogging {

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  @Qualifier("globalStatisticsMongoRepository")
  val globalStatisticsRepository: GlobalStatisticsMongoRepository = null

  @Autowired
  val statisticsService: StatisticsService = null


  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Statistics Service" should {

    "load data" in {
      spectrumMongoRepository.deleteAll()
      globalStatisticsRepository.deleteAll()

      exampleRecords.foreach(spectrumMongoRepository.save(_))
      assert(spectrumMongoRepository.count() == 50)
    }

    "perform aggregation counts" in {
      val count: Long = globalStatisticsRepository.count()

      statisticsService.updateStatistics()
      val result: GlobalStatistics = statisticsService.getGlobalStatistics

      assert(globalStatisticsRepository.count() == count + 1)

      assert(result.spectrumCount == 50)
      assert(result.compoundCount == 21)
      assert(result.metaDataCount == 21)
      assert(result.metaDataValueCount == 1050)
      assert(result.tagCount == 2)
      assert(result.tagValueCount == 100)
      assert(result.submitterCount == 1)
    }
  }
}
