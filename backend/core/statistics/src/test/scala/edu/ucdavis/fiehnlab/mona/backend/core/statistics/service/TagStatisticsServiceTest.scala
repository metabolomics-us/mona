package edu.ucdavis.fiehnlab.mona.backend.core.statistics.service

import java.io.InputStreamReader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.repository.{MetaDataStatisticsMongoRepository, TagStatisticsMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.types.{MetaDataStatistics, MetaDataValueCount, TagStatistics}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}

/**
  * Created by sajjan on 8/4/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[MongoConfig], classOf[TestConfig]))
@TestPropertySource(locations=Array("classpath:application.properties"))
class TagStatisticsServiceTest extends WordSpec with LazyLogging{

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  @Qualifier("tagStatisticsMongoRepository")
  val tagStatisticsRepository: TagStatisticsMongoRepository = null

  @Autowired
  val tagStatisticsService: TagStatisticsService = null


  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Tag Statistics Service" should {

    "load data" in {
      spectrumMongoRepository.deleteAll()
      exampleRecords.foreach(spectrumMongoRepository.save(_))
      assert(spectrumMongoRepository.count() == 58)
    }

    "perform tag aggregation" in {
      val result: Array[TagStatistics] = tagStatisticsService.tagAggregation()
      assert(result.length == 3)

      println(s"content: ${result.sortBy(_.text).map(_.text).mkString(" ")}")
      assert(result.sortBy(_.text).map(_.text) sameElements Array("LCMS", "massbank", "noisy spectra"))
      assert(result.map(_.count) sameElements Array(58, 58, 3))
    }

    "persist tag statistics" in {
      tagStatisticsRepository.deleteAll()
      tagStatisticsService.updateTagStatistics()
      assert(tagStatisticsRepository.count() == 3)
    }
  }
}
