package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging

import java.io.InputStreamReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.domain.statistics.StatisticsMetaData
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.MetaDataRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.{SpectrumResultRepository, StatisticsMetaDataRepository}
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import scala.jdk.CollectionConverters._

/**
  * Created by sajjan on 8/4/16.
 *
 * */
@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class MetaDataStatisticsServiceTest extends AnyWordSpec with LazyLogging{

  @Autowired
  private val statisticsMetaDataRepository: StatisticsMetaDataRepository = null

  @Autowired
  val metaDataStatisticsService: MetaDataStatisticsService = null

  @Autowired
  val spectrumResultsRepo: SpectrumResultRepository = null

  @Autowired
  val metaDataRepository: MetaDataRepository = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

  val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")), new TypeReference[Array[Spectrum]] {})

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "MetaData Statistics Service" should {

    "load data" in {
      spectrumResultsRepo.deleteAll()
     exampleRecords.foreach { spectrum =>
        spectrumResultsRepo.save(new SpectrumResult(spectrum.getId, spectrum))
      }
      assert(spectrumResultsRepo.count() == 59)
    }

    "generate metadata statistics" should {

      "persist metadata statistics" in {
        statisticsMetaDataRepository.deleteAll()
        metaDataStatisticsService.updateMetaDataStatistics()
        assert(statisticsMetaDataRepository.count() == 240)
      }

      "get metadata names from repository" in {
        val result: Array[StatisticsMetaData.StatisticsMetaDataSummary] = metaDataStatisticsService.getMetaDataNames
        assert(result.length == 240)
        assert(result.forall(_.getCount > 0))
      }

      "get metadata aggregation for ms level from repository" in {
        val result = metaDataStatisticsService.getMetaDataStatistics("ms level")

        assert(result.getCount == 59)
        assert(result.getMetaDataValueCount.size() == 1)
        //assert(result.get sameElements Array(MetaDataValueCount("MS2", 59)))
      }

      "get metadata aggregation for ion mode from repository" in {
        val result = metaDataStatisticsService.getMetaDataStatistics("ion mode")
        val values = result.getMetaDataValueCount.asScala.sortBy(_.getCount)


        assert(values.length == 2)
        assert(values.head.getValue == "negative")
        assert(values.head.getCount == 25)
        assert(values.last.getValue == "positive")
        assert(values.last.getCount == 33)
      }

      "ensure that the maximum count of each metadata group is the first value" in {
        metaDataStatisticsService.getMetaDataStatistics.foreach { x =>
          assert(x.getMetaDataValueCount.asScala.head.getCount == x.getMetaDataValueCount.asScala.map(_.getCount).max)
        }
      }
    }
  }
}

