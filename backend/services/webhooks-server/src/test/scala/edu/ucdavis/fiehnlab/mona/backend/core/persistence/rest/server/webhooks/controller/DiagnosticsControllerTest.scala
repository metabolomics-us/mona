package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.controller

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured._
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.webhooks.service.CloudWatchLogsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Covers the auth-gating on the diagnostics endpoints and the bad-input handling for unknown
  * service keys. Deliberately does not exercise the happy path of fetching logs or a trace, since
  * those reach into CloudWatchLogsService's real AWS client - that logic is covered without AWS
  * in CloudWatchLogsServiceSpec instead
  */
@SpringBootTest(classes = Array(classOf[TestConfig]), webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class DiagnosticsControllerTest extends AbstractSpringControllerTest {

  @LocalServerPort
  private val port = 0

  @Autowired
  val cloudWatchLogsService: CloudWatchLogsService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "a diagnostics controller" must {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    "require authentication to list known services" in {
      given().contentType("application/json; charset=UTF-8").when().get("/diagnostics/services").`then`().statusCode(401)
    }

    "require admin authority to list known services" in {
      authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/diagnostics/services").`then`().statusCode(403)
    }

    "let an admin list the known service keys" in {
      val result: java.util.Map[String, AnyRef] = authenticate().contentType("application/json; charset=UTF-8")
        .when().get("/diagnostics/services").`then`().statusCode(200).extract().body().as(classOf[java.util.Map[String, AnyRef]])

      assert(result.size() == cloudWatchLogsService.serviceStreams.size)
      assert(result.get("webhooks") == "webhooks-service")
    }

    "require authentication to fetch logs" in {
      given().contentType("application/json; charset=UTF-8").when().get("/diagnostics/logs?service=webhooks").`then`().statusCode(401)
    }

    "require admin authority to fetch logs" in {
      authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/diagnostics/logs?service=webhooks").`then`().statusCode(403)
    }

    "reject an unknown service key when fetching logs, without touching AWS" in {
      authenticate().contentType("application/json; charset=UTF-8").when().get("/diagnostics/logs?service=not-a-real-service").`then`().statusCode(400)
    }

    "require authentication to fetch a stack trace" in {
      given().contentType("application/json; charset=UTF-8").when().get("/diagnostics/logs/trace?service=webhooks&timestamp=0").`then`().statusCode(401)
    }

    "require admin authority to fetch a stack trace" in {
      authenticate("test", "test-secret").contentType("application/json; charset=UTF-8").when().get("/diagnostics/logs/trace?service=webhooks&timestamp=0").`then`().statusCode(403)
    }

    "reject an unknown service key when fetching a stack trace, without touching AWS" in {
      authenticate().contentType("application/json; charset=UTF-8").when().get("/diagnostics/logs/trace?service=not-a-real-service&timestamp=0").`then`().statusCode(400)
    }
  }
}
