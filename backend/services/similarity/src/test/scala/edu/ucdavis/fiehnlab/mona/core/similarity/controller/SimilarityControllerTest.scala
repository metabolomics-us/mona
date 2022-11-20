package edu.ucdavis.fiehnlab.mona.core.similarity.controller

import java.io.InputStreamReader
import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumRepository
import edu.ucdavis.fiehnlab.mona.core.similarity.SimilarityService
import edu.ucdavis.fiehnlab.mona.core.similarity.service.{SimilarityPopulationService, SimilarityStartupService}
import edu.ucdavis.fiehnlab.mona.core.similarity.types.AlgorithmTypes.AlgorithmType
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{IndexType, PeakSearchRequest, SearchResult, SimilaritySearchRequest}
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.hibernate.Hibernate
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
  * Created by sajjan on 5/26/16.
  */
@SpringBootTest(classes = Array(classOf[SimilarityService]), webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SimilarityControllerTest extends AnyWordSpec with Matchers with LazyLogging with BeforeAndAfterEach{

  @LocalServerPort
  private val port = 0

  @Autowired
  val postSpectrumResultRepository: SpectrumRepository = null

  @Autowired
  val similarityPopulationService: SimilarityPopulationService = null

  @Autowired
  val indexUtils: IndexUtils = null

  @Autowired
  private val transactionManager: PlatformTransactionManager = null

  private var transactionTemplate: TransactionTemplate = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  protected override def beforeEach() = (
    transactionTemplate = new TransactionTemplate(transactionManager)
    )

  protected override def afterEach() = (
    transactionTemplate = null
    )

  "SimilarityControllerTest" should {
    RestAssured.baseURI = s"http://localhost:$port/rest/similarity"

    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      transactionTemplate.execute{ x =>
        postSpectrumResultRepository.deleteAll()
        Hibernate.initialize()
        x
      }

      transactionTemplate.execute{ x =>
        exampleRecords.foreach(y => postSpectrumResultRepository.save(y))
        Hibernate.initialize()
        x
      }

      val count = transactionTemplate.execute{ x =>
        val z = postSpectrumResultRepository.count()
        Hibernate.initialize(z)
        z
      }

      assert(count == 59)
    }

    "populate the indices" in {
      similarityPopulationService.populateIndices()
      assert(indexUtils.getIndexSize == 59)
      assert(indexUtils.getIndexSize("default", IndexType.PEAK) == 170)
    }

    "list available indices" in {
      val result: Array[RestIndexDefinition] = given().contentType("application/json; charset=UTF-8").when().get("/indices").`then`().statusCode(200).extract().body().as(classOf[Array[RestIndexDefinition]])
      assert(result.length == 4)

      assert(result.exists(_.indexName == "default"))
      assert(result.filter(_.indexType.value == "DEFAULT").head.indexSize == 59)
      assert(result.filter(_.indexType.value == "PEAK").head.indexSize == 170)
    }

    "list available algorithms" in {
      val result: Array[AlgorithmType] = given().contentType("application/json; charset=UTF-8").when().get("/algorithms").`then`().statusCode(200).extract().body().as(classOf[Array[AlgorithmType]])
      assert(result.length == 6)
    }

    /* "perform a simple similarity query" in {
      val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.99)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 1)
      assert(result.head.score > 0.99)
    } */

    "perform simple similarity query with precursor removal" in {
      val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.042, 0.01, 0.0, true, "composite", false)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 1)
      assert(result.head.score > 0.99)
    }

    "perform simple similarity query with precursor removal that shouldn't match" in {
      val request: SimilaritySearchRequest = SimilaritySearchRequest("50.8086:28.55 63.0030:17.17 69.8962:8.22 71.9719:2.63 72.4053:4.71 80.3195:1.68 83.9324:176.37 84.3530:7.28 84.7319:3.61 84.9282:21.00 85.9344:60.57 99.5672:5.00 99.9257:65.58 100.0233:15.45 100.6556:2.83 100.9298:31.36 100.9697:99.02 101.0229:3.64", 0.7, 100.933979904266, 0.01, 0.0, true, "composite", false)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 0)
    }

    /* "perform a similarity query with a JSON string body with only the spectrum" in {
      val request: String = """{"spectrum": "108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100"}"""
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 1)
      assert(result.head.score > 0.99)
    } */

    /* "perform a more lax similarity query to retrieve more results" in {
      (1 to 10).foreach { i =>
        logger.info(s"Iteration $i")

        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 1)
        assert(result.head.score > 0.99)
      }
    } */

    /* "perform a similarity search on a single ion and ensure there are no overlaps" in {
      (116 to 118).foreach { mz =>
        val request: SimilaritySearchRequest = SimilaritySearchRequest(s"$mz:100", 0.9)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        if (mz == 117) {
          assert(result.exists(s => s.hit.spectrum.split(" ").exists(x => Math.abs(x.split(":").head.toDouble - 117) < 0.5)))
        } else {
          assert(!result.exists(s => s.hit.spectrum.split(" ").exists(x => Math.abs(x.split(":").head.toDouble - 117) < 0.5)))
        }
      }
    } */
    "perform search with checkAllAdducts" in {
      val request: SimilaritySearchRequest = SimilaritySearchRequest("50.2217979431:0.122755 57.0331459045:3.821643 59.0122871399:0.892777 61.969039917:0.790287 63.9611129761:1.443705 63.9641571045:0.174697 73.2538070679:0.270219 74.02318573:0.191527 74.9894943237:1.600914 84.0440292358:100.000000 85.041229248:0.788512 85.0473327637:10.973042 94.3845977783:0.167283 107.04889679:0.281357 107.600822449:0.130739 118.991203308:0.199623 119.049446106:0.528517 120.011146545:1.318132 120.019271851:0.697153 120.043907166:0.130352 124.31628418:0.138525 135.044021606:2.939773 154.268966675:0.154170 162.022125244:15.080648 162.892440796:0.166675 162.952758789:2.611380 163.00328064:0.353531 163.025238037:9.206503 163.039260864:4.082647 163.075653076:0.603174 163.111633301:1.735715 164.017913818:0.895966 178.181854248:0.158892",
        .25, 185.008091368, 0.01, 0, false, "default", true)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      logger.info(s"${result}")
      assert(result.length > 0)
      logger.info(s"${result.head.score}")
      assert(result.head.score > .99)
    }

    "perform similarity queries with precursor filtering" should {
      "use absolute tolerance" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0421, 0.01, 0, false, "composite", false)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 1)
        assert(result.head.score > 0.99)
      }

      "use tolerance in ppm" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0421, 0.0, 1, false, "composite", false)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 1)
        assert(result.head.score > 0.99)
      }
    }

    "perform similarity queries with precursor filtering and tag filtering" should {
      "use absolute tolerance with present" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0421, 0.01, 0, Array("LCMS"), Array(), false, "composite", false)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 1)
        assert(result.head.score > 0.99)
      }

      "use absolute tolerance with non-present tag" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0421, 0.01, 0, Array("GCMS"), Array(), false, "composite", false)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.isEmpty)
      }
    }

    "perform similarity queries with multi-tag filtering" should {
      /* "match multiple tags that are present" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("112.05:100", 0.25, -1, 0, 0, Array("LCMS", "massbank"), Array(), false)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 2)
        assert(result.head.score > 0.8)
      } */

      "should match no spectra when tags are not present" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("112.05:100", 0.25, -1, 0, 0, Array("LCMS", "HMDB"), Array(), false, "composite", false)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 0)
      }

      /*"should match spectra with filter tags" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("112.05:100", 0.25, -1, 0, 0, Array("LCMS"), Array("massbank", "HMDB"), false)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 2)
        assert(result.head.score > 0.8)
      } */
    }

    "perform a simple peak search" in {
      val request: PeakSearchRequest = PeakSearchRequest(Array(108, 126), 1)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/peakSearch").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 4)
      assert(result.forall(_.score == 1))
    }

    "perform a simple peak search no hits if a large ion is included" in {
      val request: PeakSearchRequest = PeakSearchRequest(Array(108, 126, 1000), 1)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/peakSearch").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 0)
    }

    "perform a simple peak search with a lower tolerance" in {
      val request: PeakSearchRequest = PeakSearchRequest(Array(108, 126), 0.1)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/peakSearch").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 4)
      assert(result.forall(_.score == 1))
    }

    "perform a peak search with a JSON string body and without a tolerance" in {
      val request: String = """{"peaks": [108, 126]}"""
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/peakSearch").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 4)
      assert(result.forall(_.score == 1))
    }
  }
}

case class RestIndexDefinition(indexType: RestIndexTypeDefinition, indexName: String, indexSize: Int)

case class RestIndexTypeDefinition(enumClass: String, value: String)
