package edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.aggregation

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.{ MongoConfig}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.repository.aggregation.IStatisticsMongoRepository
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{TestPropertySource, ContextConfiguration, TestContextManager}

/**
  * This test extends `edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.SpectrumMongoRepositoryCustomTest`
  * to obtain the same test data.
  *
  * This ensures that the tests below are consistent with the spectrum (and RSQL) database structure. If the sample data,
  * schema or data handling in those repositories change these tests *might* fail, in which case the statistical
  * algorithms should be re-evaluated.
  *
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[MongoConfig], classOf[TestConfig]))
@TestPropertySource(locations=Array("classpath:application.properties"))
class StatisticsMongoRepositoryTest extends WordSpec {

  @Autowired
  @Qualifier("statisticsMongoRepository")
  val statisticsMongoRepository: IStatisticsMongoRepository = null

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "Metadata aggregation queries" should  {

    spectrumMongoRepository.deleteAll()
    exampleRecords.foreach(spectrumMongoRepository.save(_))

    "given a specific metadata field" must {
      val msLevel = statisticsMongoRepository.aggregateByName("ms level")
      val ionMode = statisticsMongoRepository.aggregateByName("ion mode")

      "return the top records in the base metadata group" in {
        assert(msLevel.nonEmpty)
        assert(msLevel == ("MS2",58) :: Nil)

        assert(ionMode.nonEmpty)
        assert(ionMode == ("positive",33) :: ("negative",25) :: Nil)
      }

      "throw an exception when given a null metadata field name" in {
        intercept[IllegalArgumentException] {
          statisticsMongoRepository.aggregateByName(null)
        }
      }
    }

    "given a specific metadata field and metadata group" must {
      val totalExactMass = statisticsMongoRepository.aggregateByName("total exact mass", metaDataGroup = Some("biological"))
      val bioCyc = statisticsMongoRepository.aggregateByName("BioCyc", metaDataGroup = Some("biological"))

      "return the top records for the metadata field/group" in {
        assert(totalExactMass.nonEmpty)
        assert(bioCyc.nonEmpty)
      }

      "return the records sorted by total count, then by name" in {
        def testPairs[T](xs: Iterable[T])(f: (T, T) => Boolean): Boolean =
          (xs.init zip xs.tail).map(x => f(x._1, x._2)).foldLeft(true)(_ && _)

        def checkSorting(result: Iterable[(AnyVal, Int)]) = {
          // Check that results are sorted by count
          val counts: Iterable[Int] = result.map(_._2)
          assert(testPairs(counts)((a, b) => a >= b))

          // Group results by value, and check if groups are sorted by name
          val groups = result.groupBy(_._2).map(_._2)
          groups.foreach {
            group =>
              val names: Iterable[String] = group.map(_._1.toString)
              assert(testPairs(names)((a, b) => a <= b))
          }
        }

        checkSorting(totalExactMass)
        checkSorting(bioCyc)

      }

      "produce empty results if given non-existent metadata groups or name" in {
        assert(statisticsMongoRepository.aggregateByName("non-existent").isEmpty)

        assert(statisticsMongoRepository.aggregateByName("non-existent", metaDataGroup = Some("non-existent")).isEmpty)
      }
    }
  }
}
