package edu.ucdavis.fiehnlab.mona.core.similarity.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.core.similarity.config.SimilarityConfig
import edu.ucdavis.fiehnlab.mona.core.similarity.index.IndexRegistry
import edu.ucdavis.fiehnlab.mona.core.similarity.types._
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{ComponentScan, Configuration}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 5/26/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TestConfig], classOf[SimilarityConfig]))
class SimilaritySearchServiceTest extends WordSpec with Matchers with LazyLogging {

  @Autowired
  val similaritySearchService: SimilaritySearchService = null

  @Autowired
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val indexUtils: IndexUtils = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SimilarityControllerTest" should {

    "not return spectra with neighboring ions as valid matches" must {
      // Populate the database
      val testSpectrum: String = "117:100"

      val correctMatches: Array[StoredSpectrum] = Array(
        StoredSpectrum("EA000457", "117.0349:100.000000"),
        StoredSpectrum("KO000331", "59.100:0.015541 73.000:1.221521 74.700:0.012433 88.800:0.012433 98.900:0.282846 116.600:7.897927 117.000:100.000000"),
        StoredSpectrum("UF022924", "90.0464:0.944412 116.0492:0.111331 117.0572:100.000000")
      )

      val incorrectMatches: Array[StoredSpectrum] = Array(
        StoredSpectrum("KO001988", "71.900:0.121830 97.300:0.033226 116.200:100.000000 133.800:0.055377"),
        StoredSpectrum("KO001507", "59.300:0.194558 68.000:0.008221 70.300:0.063026 71.800:0.016442 98.200:0.021922 116.200:100.000000 133.800:0.021922"),
        StoredSpectrum("KO000283", "59.000:0.680911 116.200:100.000000"),
        StoredSpectrum("KO001989", "44.800:0.241546 70.100:0.966184 72.100:0.966184 78.600:0.161031 116.100:100.000000")
      )

      val totalSpectra: Int = correctMatches.length + incorrectMatches.length

      "load some data" in {
        spectrumMongoRepository.deleteAll()

        (correctMatches ++ incorrectMatches).foreach { x =>
          val spectrum: Spectrum = Spectrum(null, x.id, null, null, null, null, null, null, x.spectrum, null, null, null, null, null)
          spectrumMongoRepository.save(spectrum)
        }
      }

      "load some data into the default index" in {
        (correctMatches ++ incorrectMatches).map(new SimpleSpectrum(_)).foreach(indexUtils.addToIndex)
        assert(indexUtils.getIndexSize == totalSpectra)
      }

      "should properly find matching spectra and not return results with adjacent ions" in {
        val hits: Array[SearchResult] = similaritySearchService.search(SimilaritySearchRequest(testSpectrum, 0.9), totalSpectra)

        assert(hits.length == 3)
        assert(hits.map(_.hit.id).sorted.sameElements(correctMatches.map(_.id).sorted))
      }
    }
  }
}

@Configuration
@ComponentScan(basePackageClasses = Array(classOf[MongoConfig], classOf[SimilaritySearchService], classOf[IndexUtils], classOf[IndexRegistry]))
class TestConfig