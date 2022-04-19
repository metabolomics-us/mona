package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.CompoundMetaDataStatisticsMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.service.CompoundMetaDataStatisticsService
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{CompoundMetaDataStatistics, CompoundMetaDataStatisticsSummary, CompoundMetaDataValueCount}
import org.junit.runner.RunWith
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}

@RunWith(classOf[SpringRunner])
@ContextConfiguration(classes = Array(classOf[MongoConfig], classOf[TestConfig]))
@TestPropertySource(locations = Array("classpath:application.properties"))
class CompoundMetaDataStatisticsServiceTest extends AnyWordSpec {

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  @Qualifier("compoundMetadataStatisticsMongoRepository")
  val compoundMetaDataStatisticsRepository: CompoundMetaDataStatisticsMongoRepository = null

  @Autowired
  val compoundMetaDataStatisticsService: CompoundMetaDataStatisticsService = null


  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MetaData Statistics Service" should {

    "load data" in {
      spectrumMongoRepository.deleteAll()
      exampleRecords.foreach(spectrumMongoRepository.save(_))
      assert(spectrumMongoRepository.count() == 59)
    }

    "perform metadata name aggregation" in {
      val result: Array[CompoundMetaDataStatisticsSummary] = compoundMetaDataStatisticsService.compoundMetaDataNameAggregation()
      assert(result.length == 59)
      assert(result.forall(_.count > 0))
    }


    "generate metadata statistics" should {

      "persist metadata statistics" in {
        compoundMetaDataStatisticsRepository.deleteAll()
        compoundMetaDataStatisticsService.updateCompoundMetaDataStatistics()

        assert(compoundMetaDataStatisticsRepository.count() == 179)
      }

      "get metadata names from repository" in {
        val result: Array[CompoundMetaDataStatisticsSummary] = compoundMetaDataStatisticsService.getCompoundMetaDataNames
        assert(result.length == 179)
        assert(result.forall(_.count > 0))
      }

      "get metadata aggregation for ms level from repository" in {
        val result = compoundMetaDataStatisticsService.getCompoundMetaDataStatistics("InChI")

        assert(result.count == 59)
        assert(result.values.length == 59)
        assert(result.values sameElements Array(CompoundMetaDataValueCount("InChI", 59)))
      }


      "re-persist metadata statistics with a slice limit of 5" in {
        compoundMetaDataStatisticsRepository.deleteAll()
        compoundMetaDataStatisticsService.updateCompoundMetaDataStatistics(5)

        assert(compoundMetaDataStatisticsRepository.count() == 179)
      }

      "ensure that each metadata group has at most 5 values" in {
        compoundMetaDataStatisticsService.getCompoundMetaDataStatistics.foreach { x =>
          assert(x.values.head.count == x.values.map(_.count).max)
        }
      }

      "ensure that the maximum count of each metadata group is the first value" in {
        compoundMetaDataStatisticsService.getCompoundMetaDataStatistics.foreach { x =>
          assert(x.values.length <= 5)
        }
      }
    }
  }
}
