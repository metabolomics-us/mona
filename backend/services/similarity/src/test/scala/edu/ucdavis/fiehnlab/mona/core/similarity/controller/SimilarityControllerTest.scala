package edu.ucdavis.fiehnlab.mona.core.similarity.controller

import java.io.InputStreamReader

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.core.similarity.SimilarityService
import edu.ucdavis.fiehnlab.mona.core.similarity.service.SimilarityStartupService
import edu.ucdavis.fiehnlab.mona.core.similarity.types.AlgorithmTypes.AlgorithmType
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{IndexType, PeakSearchRequest, SearchResult, SimilaritySearchRequest}
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.junit.runner.RunWith
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 5/26/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[SimilarityService]), webEnvironment = WebEnvironment.DEFINED_PORT)
class SimilarityControllerTest extends AnyWordSpec with Matchers with LazyLogging {

  @LocalServerPort
  private val port = 0

  @Autowired
  val mongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val similarityStartupService: SimilarityStartupService = null

  @Autowired
  val indexUtils: IndexUtils = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SimilarityControllerTest" should {
    RestAssured.baseURI = s"http://localhost:$port/rest/similarity"

    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      mongoRepository.deleteAll()
      exampleRecords.foreach(mongoRepository.save(_))

      assert(mongoRepository.count() == 59)
    }

    "populate the indices" in {
      similarityStartupService.populateIndices()
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
      assert(result.length == 5)
    }

    /* "perform a simple similarity query" in {
      val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.99)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 1)
      assert(result.head.score > 0.99)
    } */

    "perform simple similarity query with precursor removal" in {
      val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.042, 0.01, 0.0, true, "composite")
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 1)
      assert(result.head.score > 0.99)
    }

    "perform simple similarity query with precursor removal that shouldn't match" in {
      val request: SimilaritySearchRequest = SimilaritySearchRequest("50.8086:28.55 63.0030:17.17 69.8962:8.22 71.9719:2.63 72.4053:4.71 80.3195:1.68 83.9324:176.37 84.3530:7.28 84.7319:3.61 84.9282:21.00 85.9344:60.57 99.5672:5.00 99.9257:65.58 100.0233:15.45 100.6556:2.83 100.9298:31.36 100.9697:99.02 101.0229:3.64", 0.7, 100.933979904266, 0.01, 0.0, true, "composite")
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

    "perform similarity queries with precursor filtering" should {
      "use absolute tolerance" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0421, 0.01, 0, false, "composite")
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 1)
        assert(result.head.score > 0.99)
      }

      "use tolerance in ppm" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0421, 0.0, 1, false, "composite")
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 1)
        assert(result.head.score > 0.99)
      }
    }

    "perform similarity queries with precursor filtering and tag filtering" should {
      "use absolute tolerance with present" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0421, 0.01, 0, Array("LCMS"), Array(), false, "composite")
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").`then`().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 1)
        assert(result.head.score > 0.99)
      }

      "use absolute tolerance with non-present tag" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0421, 0.01, 0, Array("GCMS"), Array(), false, "composite")
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
        val request: SimilaritySearchRequest = SimilaritySearchRequest("112.05:100", 0.25, -1, 0, 0, Array("LCMS", "HMDB"), Array(), false, "composite")
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
