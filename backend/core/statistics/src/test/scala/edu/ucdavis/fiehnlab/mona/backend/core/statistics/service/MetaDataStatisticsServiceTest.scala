package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.MetaDataStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, MetaDataStatisticsSummary, MetaDataValueCount}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}

import scala.collection.JavaConverters._

/**
  * Created by sajjan on 8/4/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[MongoConfig], classOf[TestConfig]))
@TestPropertySource(locations=Array("classpath:application.properties"))
class MetaDataStatisticsServiceTest extends WordSpec {

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  @Qualifier("metadataStatisticsMongoRepository")
  val metaDataStatisticsRepository: MetaDataStatisticsMongoRepository = null

  @Autowired
  val metaDataStatisticsService: MetaDataStatisticsService = null


  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MetaData Statistics Service" should {

    "load data" in {
      spectrumMongoRepository.deleteAll()
      exampleRecords.foreach(spectrumMongoRepository.save(_))
      assert(spectrumMongoRepository.count() == 58)
    }

    "perform metadata name aggregation" in {
      val result: Array[MetaDataStatisticsSummary] = metaDataStatisticsService.metaDataNameAggregation()
      assert(result.length == 44)
      assert(result.forall(_.count > 0))
    }

    "perform metadata aggregation for ms level" in {
      val result: MetaDataStatistics = metaDataStatisticsService.metaDataAggregation("ms level")

      assert(result.count == 58)
      assert(result.values.length == 1)
      assert(result.values sameElements Array(MetaDataValueCount("MS2", 58)))
    }

    "perform metadata aggregation for ion mode" in {
      val result: MetaDataStatistics = metaDataStatisticsService.metaDataAggregation("ion mode")

      assert(result.count == 58)
      assert(result.values.length == 2)
      assert(result.values sameElements Array(MetaDataValueCount("positive", 33), MetaDataValueCount("negative", 25)))
    }

    "generate metadata statistics" should {

      "persist metadata statistics" in {
        metaDataStatisticsRepository.deleteAll()
        metaDataStatisticsService.updateMetaDataStatistics()

        assert(metaDataStatisticsRepository.count() == 44)
      }

      "get metadata names from repository" in {
        val result: Array[MetaDataStatisticsSummary] = metaDataStatisticsService.getMetaDataNames
        assert(result.length == 44)
        assert(result.forall(_.count > 0))
      }

      "get metadata aggregation for ms level from repository" in {
        val result = metaDataStatisticsService.getMetaDataStatistics("ms level")

        assert(result.count == 58)
        assert(result.values.length == 1)
        assert(result.values sameElements Array(MetaDataValueCount("MS2", 58)))
      }

      "get metadata aggregation for ion mode from repository" in {
        val result = metaDataStatisticsService.getMetaDataStatistics("ion mode")
        val values = result.values.sortBy(_.count)


        assert(values.length == 2)
        assert(values.head == MetaDataValueCount("negative", 25))
        assert(values.last == MetaDataValueCount("positive", 33))
      }

      "re-persist metadata statistics with a slice limit of 5" in {
        metaDataStatisticsRepository.deleteAll()
        metaDataStatisticsService.updateMetaDataStatistics(5)

        assert(metaDataStatisticsRepository.count() == 44)
      }

      "ensure that each metadata group has at most 5 values" in {
        metaDataStatisticsService.getMetaDataStatistics.asScala.foreach { x =>
          assert(x.values.head.count == x.values.map(_.count).max)
        }
      }

      "ensure that the maximum count of each metadata group is the first value" in {
        metaDataStatisticsService.getMetaDataStatistics.asScala.foreach { x =>
          assert(x.values.length <= 5)
        }
      }
    }
  }
}
