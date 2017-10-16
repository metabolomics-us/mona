package edu.ucdavis.fiehnlab.mona.core.similarity.controller

import java.io.InputStreamReader

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.core.similarity.SimilarityService
import edu.ucdavis.fiehnlab.mona.core.similarity.index.PeakIndex
import edu.ucdavis.fiehnlab.mona.core.similarity.service.SimilarityStartupService
import edu.ucdavis.fiehnlab.mona.core.similarity.types.AlgorithmTypes.AlgorithmType
import edu.ucdavis.fiehnlab.mona.core.similarity.types.{IndexType, PeakSearchRequest, SearchResult, SimilaritySearchRequest}
import edu.ucdavis.fiehnlab.mona.core.similarity.util.IndexUtils
import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.{SpringApplicationConfiguration, WebIntegrationTest}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 5/26/16.
  */
@WebIntegrationTest(Array("server.port=9999", "eureka.client.enabled:false"))
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[SimilarityService]))
class SimilarityControllerTest extends WordSpec with Matchers with LazyLogging {

  val port: Int = 9999

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

      for (spectrum <- exampleRecords) {
        mongoRepository.save(spectrum)
      }

      assert(mongoRepository.count() == 58)
    }

    "populate the indices" in {
      similarityStartupService.populateIndices()
      assert(indexUtils.getIndexSize == 58)
      assert(indexUtils.getIndexSize("default", IndexType.PEAK) == 162)
    }

    "list available indices" in {
      val result: Array[RestIndexDefinition] = given().contentType("application/json; charset=UTF-8").when().get("/indices").then().statusCode(200).extract().body().as(classOf[Array[RestIndexDefinition]])
      assert(result.length == 4)

      assert(result.exists(_.indexName == "default"))
      assert(result.filter(_.indexType.value == "DEFAULT").head.indexSize == 58)
      assert(result.filter(_.indexType.value == "PEAK").head.indexSize == 162)
    }

    "list available algorithms" in {
      val result: Array[AlgorithmType] = given().contentType("application/json; charset=UTF-8").when().get("/algorithms").then().statusCode(200).extract().body().as(classOf[Array[AlgorithmType]])
      assert(result.length == 5)
    }

    "perform a simple similarity query" in {
      val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.99)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 1)
      assert(result.head.score > 0.99)
    }

    "perform a similarity query with a JSON string body with only the spectrum" in {
      val request: String = """{"spectrum": "108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100"}"""
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 1)
      assert(result.head.score > 0.99)
    }

    "perform a more lax similarity query to retrieve more results" in {
      (1 to 10).foreach { i =>
        logger.info(s"Iteration $i")

        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 1)
        assert(result.head.score > 0.99)
      }
    }

    "perform a similarity search on a single ion and ensure there are no overlaps" in {
      (116 to 118).foreach { mz =>
        val request: SimilaritySearchRequest = SimilaritySearchRequest(s"$mz:100", 0.9)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        if (mz == 117) {
          assert(result.exists(s => s.hit.spectrum.split(" ").exists(x => Math.abs(x.split(":").head.toDouble - 117) < 0.5)))
        } else {
          assert(!result.exists(s => s.hit.spectrum.split(" ").exists(x => Math.abs(x.split(":").head.toDouble - 117) < 0.5)))
        }
      }
    }

    "perform similarity queries with precursor filtering" should {
      "use absolute tolerance" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0421, 0.01, 0)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 1)
        assert(result.head.score > 0.99)
      }

      "use tolerance in ppm" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0421, 0.0, 1)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.length == 1)
        assert(result.head.score > 0.99)
      }

      "fail if no match exists for absolute tolerance" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0221, 0.01, 0)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.isEmpty)
      }

      "fail if no match exists for ppm tolerance" in {
        val request: SimilaritySearchRequest = SimilaritySearchRequest("108.0204:4.934837 126.0308:0.502892 133.0156:34.528632 150.042:100", 0.25, 150.0221, 0.0, 1)
        val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/search").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

        assert(result.isEmpty)
      }
    }


    "perform a simple peak search" in {
      val request: PeakSearchRequest = PeakSearchRequest(Array(108, 126), 1)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/peakSearch").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 4)
      assert(result.forall(_.score == 1))
    }

    "perform a simple peak search no hits if a large ion is included" in {
      val request: PeakSearchRequest = PeakSearchRequest(Array(108, 126, 1000), 1)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/peakSearch").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 0)
    }

    "perform a simple peak search with a lower tolerance" in {
      val request: PeakSearchRequest = PeakSearchRequest(Array(108, 126), 0.1)
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/peakSearch").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 4)
      assert(result.forall(_.score == 1))
    }

    "perform a peak search with a JSON string body and without a tolerance" in {
      val request: String = """{"peaks": [108, 126]}"""
      val result: Array[SearchResult] = given().contentType("application/json; charset=UTF-8").body(request).when().post("/peakSearch").then().statusCode(200).extract().body().as(classOf[Array[SearchResult]])

      assert(result.length == 4)
      assert(result.forall(_.score == 1))
    }
  }
}

case class RestIndexDefinition(indexType: RestIndexTypeDefinition, indexName: String, indexSize: Int)
case class RestIndexTypeDefinition(enumClass: String, value: String)