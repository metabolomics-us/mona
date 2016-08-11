package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.{MetaDataStatisticsMongoRepository, TagStatisticsMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, TagStatistics}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 8/4/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[MongoConfig], classOf[TestConfig]))
@TestPropertySource(locations=Array("classpath:application.properties"))
class StatisticsServiceTest extends WordSpec {

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  @Qualifier("metadataStatisticsMongoRepository")
  val metaDataStatisticsRepository: MetaDataStatisticsMongoRepository = null

  @Autowired
  @Qualifier("tagStatisticsMongoRepository")
  val tagStatisticsRepository: TagStatisticsMongoRepository = null

  @Autowired
  val statisticsService: StatisticsService = null

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Statistics Service" should {

    spectrumMongoRepository.deleteAll()
    exampleRecords.foreach(spectrumMongoRepository.save(_))

    "perform metadata name aggregation" in {
      val result: Array[String] = statisticsService.metaDataNameAggregation()
      assert(result.length == 44)
    }

    "perform metadata aggregation" in {
      val result: Array[MetaDataStatistics] = statisticsService.metaDataAggregation()
      assert(result.length == 44)

      assert(result.filter(_.name == "ms level").head.values sameElements Array(("MS2", 58)))
      assert(result.filter(_.name == "ion mode").head.values sameElements Array(("positive", 33), ("negative", 25)))
    }

    "persist metadata statistics" in {
      metaDataStatisticsRepository.deleteAll()
      statisticsService.updateMetaDataStatistics()
      assert(metaDataStatisticsRepository.count() == 44)
    }


    "perform tag aggregation" in {
      val result: Array[TagStatistics] = statisticsService.tagAggregation()
      assert(result.length == 3)
      assert(result.map(_.text) sameElements Array("massbank", "LCMS", "noisy spectra"))
      assert(result.map(_.count) sameElements Array(58, 58, 3))
    }

    "persist tag statistics" in {
      tagStatisticsRepository.deleteAll()
      statisticsService.updateTagStatistics()
      assert(tagStatisticsRepository.count() == 3)
    }
  }
}
