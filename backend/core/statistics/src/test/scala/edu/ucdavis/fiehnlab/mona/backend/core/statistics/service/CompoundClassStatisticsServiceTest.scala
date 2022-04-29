package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.TestConfig
import org.junit.runner.RunWith
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}

/**
  * Created by sajjan on 8/4/16.
  */
@RunWith(classOf[SpringRunner])
@DataMongoTest
@ContextConfiguration(classes = Array(classOf[MongoConfig], classOf[TestConfig]))
@TestPropertySource(locations = Array("classpath:application.properties"))
class CompoundClassStatisticsServiceTest extends AnyWordSpec {

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val compoundClassStatisticsService: CompoundClassStatisticsService = null

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Compound Class Statistics Service" should {

    "load data" in {
      spectrumMongoRepository.deleteAll()
      exampleRecords.foreach(spectrumMongoRepository.save(_))
      assert(spectrumMongoRepository.count() == 50)
    }

    "perform aggregation" in {
      compoundClassStatisticsService.updateCompoundClassStatistics()

      assert(compoundClassStatisticsService.countCompoundClassStatistics == 5)

      assert(compoundClassStatisticsService.getCompoundClassStatistics("Organic compounds").spectrumCount == 50)
      assert(compoundClassStatisticsService.getCompoundClassStatistics("Organic compounds").compoundCount == 21)
    }
  }
}
