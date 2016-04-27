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
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

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
class StatisticsMongoRepositoryTest extends WordSpec {

  @Autowired
  @Qualifier("statisticsMongoRepository")
  val statisticsMongoRepository: IStatisticsMongoRepository = null

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "Metadata aggregation queries" ignore  {

    spectrumMongoRepository.deleteAll()
    exampleRecords.foreach(spectrumMongoRepository.save(_))

    "given a specific metadata field" must {
      "return the top records in the base metadata group" in {
        val msLevel = statisticsMongoRepository.aggregateByName("ms level")
        assert(msLevel.nonEmpty)
        assert(msLevel == ("MS2",58) :: Nil)

        val ionMode = statisticsMongoRepository.aggregateByName("ion mode")
        assert(ionMode.nonEmpty)
        assert(ionMode == ("positive",33) :: ("negative",25) :: Nil)
      }

      "return the records sorted by total count, then by name" in {
        val expected = Seq(
          ("CPD-10411",2),
          ("CPD-592",2),
          ("CPD-8097",2),
          ("CYTIDINE",2),
          ("ECTOINE",2),
          ("GUANINE",2),
          ("4-HYDROXYBENZALDEHYDE",1),
          ("5-METHYLTHIOADENOSINE",1),
          ("8-HYDROXYQUINOLINE",1),
          ("BETAINE",1),
          ("CYTOSINE",1),
          ("METHYLNICOTINATE",1),
          ("NIACINAMIDE",1),
          ("URIDINE",1),
          ("VANILLIN",1))

        val result = statisticsMongoRepository.aggregateByName("BioCyc", metaDataGroup = Some("compound"))
        assert(result == expected)
      }

      "throw an exception when given a null metadata field name" in {
        intercept[IllegalArgumentException] {
          statisticsMongoRepository.aggregateByName(null)
        }
      }
    }

    "given a specific metadata field and metadata group" must {
      "return the top records for the metadata field/group" in {
        val result = statisticsMongoRepository.aggregateByName("total exact mass", metaDataGroup = Some("compound"))
        assert(result.nonEmpty)
      }
    }
  }
}
