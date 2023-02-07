package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api

import java.io.InputStreamReader
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Roles, Spectrum, Users}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumRepository
import org.scalatest.concurrent.Eventually
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestContextManager
import org.springframework.web.client.HttpClientErrorException

import scala.jdk.CollectionConverters._
import scala.concurrent.duration._
import scala.language.postfixOps


/**
  * Created by wohlgemuth on 3/2/16.
  */
abstract class AbstractRestClientTest extends AnyWordSpec with Eventually with LazyLogging with Matchers {

  @Autowired
  val spectrumRestClient: GenericRestClient[Spectrum,  String] = null

  @Autowired
  val userRepo: UserRepository = null

  @Autowired
  val spectrumRepository: SpectrumRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  /**
    * some test data to work with
    */
  "when we start a client" when {

    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "we must have a user first " must {
      "create user " in {
        userRepo.deleteAll()
        userRepo.save(new Users("admin", "secret", List(new Roles("ADMIN")).asJava))
      }

      "we should be able to login" in {
        spectrumRestClient.login("admin", "secret")
      }

      "we should be able to delete all data" in {
        spectrumRestClient.list().foreach { x =>
          spectrumRestClient.delete(x.getId)
        }
      }

     "we should be able to add spectra" in {
        exampleRecords.foreach{ x =>
          spectrumRestClient.add(x)
        }
      }

      "we should have 59 spectra" in {
        eventually(timeout(10 seconds)) {
          assert(spectrumRestClient.count() == 59)
        }
      }

      "it should be possible to execute count queries" in {
        val test = spectrumRestClient.list().head
        assert(spectrumRestClient.count(Some(s"id:'${test.getId}'")) == 1)
      }

      "it should be possible to update values" in {
        val test = spectrumRestClient.list().toList.head
        val result = spectrumRestClient.update(test, "newTestId")

        assert(result.getId == "newTestId")
        assert(spectrumRestClient.get("newTestId").getId == "newTestId")
      }

      "it should be possible to get values" in {
        val records = spectrumRestClient.list().toList
        val spectrum = spectrumRestClient.get(records.head.getId)

        assert(spectrum.getId == records.head.getId)
      }

      "it should be possible to list all values" in {
        val count = spectrumRestClient.list().foldLeft(0)((sum, _) => sum + 1)
        assert(count == 59)
      }


      "it should be possible to stream all values" in {
        val count = spectrumRestClient.stream(None).foldLeft(0)((sum, _) => sum + 1)
        assert(count == 59)
      }

      "it should be possible to stream all values with no duplicate ids" in {
        val count = spectrumRestClient.stream(None).map(_.getId).toSet.size
        assert(count == 59)
      }

      "it should be possible to paginate" in {
        val count = spectrumRestClient.list(pageSize = Some(10)).foldLeft(0)((sum, _) => sum + 1)
        assert(count == 10)
      }

      "possible to execute the same query several times and receive always the same result" must {
        for (x <- 1 to 10) {
          s"support pageable sizes of $x" in {
            var last: Iterable[Spectrum] = null

            for (_ <- 1 to 25) {
              val current: Iterable[Spectrum] = spectrumRestClient.list(query = Option("tags.text~'LCMS'"), pageSize = Option(x), page = Option(0))
              logger.info(s"${current}")
              if (last == null) {
                last = current
              }

              assert(current.size == last.size)
              assert(current.size == x)

              var run = false

              (current zip last).foreach { s: (Spectrum, Spectrum) =>
                logger.info(s"comparing ${s._1.getId} to ${s._2.getId}")
                run = true
                assert(s._1.getId == s._2.getId)
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
        val data = spectrumRestClient.list(Some("tags.text~'*LCMS*'"))
        assert(data.toList.length == 58)
      }

      "it should be possible to delete values" in {
        val records = spectrumRestClient.list()
        val countBefore = spectrumRestClient.count()

        spectrumRestClient.delete(records.head.getId)

        eventually(timeout(10 seconds)) {
          val countAfter = spectrumRestClient.count()

          assert(countBefore - countAfter == 1)
        }
      }

      "to query none existing data should result in a 404" in {
        val thrown = intercept[HttpClientErrorException] {
          spectrumRestClient.get("I don't Exist And I Like Beer, but whiskey is not bad either")
        }

        logger.info(s"${thrown.getMessage}")
        assert(thrown.getMessage == "404 : [no body]")
      }
    }
  }
}
