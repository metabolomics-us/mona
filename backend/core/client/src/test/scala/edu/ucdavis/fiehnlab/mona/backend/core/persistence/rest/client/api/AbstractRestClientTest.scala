package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api
import scala.concurrent.duration._
import java.io.InputStreamReader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.{Role, User}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.{ShouldMatchers, WordSpec}
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.TestContextManager
import org.springframework.web.client.HttpClientErrorException

import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/2/16.
  */
abstract class AbstractRestClientTest extends WordSpec with Eventually with LazyLogging with ShouldMatchers {
  @Value( """${local.server.port}""")
  val port: Int = 0

  @Autowired
  val spectrumRestClient: GenericRestClient[Spectrum, String] = null

  @Autowired
  val userRepo: UserRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  /**
    * some test data to work with
    */
  "when we start a client" when {

    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "we must have a user first " must {
      "create user " in {
        userRepo.deleteAll()
        userRepo.save(User("admin", "secret", Array(Role("ADMIN")).toList.asJava))
      }

      "we should be able to login" in {
        spectrumRestClient.login("admin", "secret")
      }

      "we should be able to delete all data" in {
        spectrumRestClient.list().foreach(x =>
          spectrumRestClient.delete(x.id)
        )
      }

      "we should be able to add spectra" in {
        for (spec <- exampleRecords) {
          spectrumRestClient.add(spec)
        }
      }

      "we should have 58 spectra" in {
        eventually(timeout(10 seconds)) {
          assert(spectrumRestClient.count() == 58)
        }
      }

      "it should be possible to execute count queries" in {
        val test = spectrumRestClient.list().head
        assert(spectrumRestClient.count(Some(s"""id==${test.id}""")) == 1)
      }

      "it should be possible to update values" in {
        val test = spectrumRestClient.list().toList.head
        val result = spectrumRestClient.update(test, "newTestId")

        assert(result.id == "newTestId")
        assert(spectrumRestClient.get("newTestId").id == "newTestId")
      }

      "it should be possible to get values" in {
        val records = spectrumRestClient.list().toList
        val spectrum = spectrumRestClient.get(records.head.id)

        assert(spectrum.id == records.head.id)
      }

      "it should be possible to list all values" in {
        val count = spectrumRestClient.list().foldLeft(0)((sum, _) => sum + 1)
        assert(count == 58)
      }


      "it should be possible to stream all values" in {
        val count = spectrumRestClient.stream(None).foldLeft(0)((sum, _) => sum + 1)
        assert(count == 58)
      }



      "it should be possible to paginate" in {

        val count = spectrumRestClient.list(pageSize = Some(10)).foldLeft(0)((sum, _) => sum + 1)
        assert(count == 10)
      }


      "possible to execute the same query several times and receive always the same result" must {
        for( x <- 1 to 10) {
          s"support pageable sizes of $x" in {
            var last: Iterable[Spectrum] = null

            for (_ <- 1 to 25) {
              val current: Iterable[Spectrum] = spectrumRestClient.list(query = Option("tags=q='text=match=\"[(LCMS)(lcms)]+\"'"), pageSize = Option(x), page = Option(0))

              if (last == null) {
                last = current
              }

              assert(current.size == last.size)
              assert(current.size == x)

              var run = false

              (current zip last).foreach { s: (Spectrum, Spectrum) =>
                logger.info(s"comparing ${s._1.id} to ${s._2.id}")
                run = true
                assert(s._1.id == s._2.id)
              }

              logger.info("")
              assert(run)
            }
          }
        }
      }

      "it should be possible to paginate over several pages" in {
        val dataFirst = spectrumRestClient.list(pageSize = Some(10), page = Some(0)).toList
        val dataSecond = spectrumRestClient.list(pageSize = Some(10), page = Some(1)).toList

        assert(dataFirst.length == 10)
        assert((dataFirst.toSet diff dataSecond.toSet).size == 10)
      }

      "it should be possible to execute queries" in {
        val data = spectrumRestClient.list(Some(""" tags=q='text==LCMS' """))
        assert(data.toList.length == exampleRecords.length)
      }

      "it should be possible to execute queries with regular expressions" in {
        val data = spectrumRestClient.list(Some(""" tags=q='text=match="[(lcms)(LCMS)]+"' """))
        assert(data.toList.length == exampleRecords.length)
      }

      "it should be possible to delete values" in {
        val records = spectrumRestClient.list()
        val countBefore = spectrumRestClient.count()

        spectrumRestClient.delete(records.head.id)

        eventually(timeout(10 seconds)) {
          val countAfter = spectrumRestClient.count()

          assert(countBefore - countAfter == 1)
        }
      }

      "to query none existing data should result in a 404" in {

        val thrown = intercept[HttpClientErrorException] {
          spectrumRestClient.get("I don't Exist And I Like Beer, but whiskey is not bad either")
        }

        assert(thrown.getMessage == "404 Not Found")
      }
    }
  }
}