package edu.ucdavis.fiehnlab.mona.backend.core.curation.controller

import com.jayway.restassured.RestAssured
import com.jayway.restassured.RestAssured.given
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.MonaNotificationBusCounterConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.curation.CurationScheduler
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.WrappedString
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 12/11/2016.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[CurationScheduler], classOf[MonaNotificationBusCounterConfiguration]), webEnvironment = WebEnvironment.DEFINED_PORT)
class CompoundConversionControllerTest extends AbstractSpringControllerTest with Eventually {

  @LocalServerPort
  private val port = 0

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CurationControllerTest" should {

    RestAssured.baseURI = s"http://localhost:$port/rest"

    "these must all fail, since we require to be logged in " must {
      "process SMILES" in {
        given().contentType("application/json; charset=UTF-8").when().body(WrappedString("CCCCCCC")).post("/conversion/smiles").`then`().statusCode(200)
      }

      "process InChI" in {
        given().contentType("application/json; charset=UTF-8").when().body(WrappedString("InChI=1S/C7H16/c1-3-5-7-6-4-2/h3-7H2,1-2H3")).post("/conversion/inchi").`then`().statusCode(200)
      }

      "process MOL" in {
        val molData: String = "\n  CDK     1218171405\n\n  7  6  0  0  0  0  0  0  0  0999 V2000\n    0.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    7.7942   -0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    1.2990   -0.7500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    6.4952   -0.7500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    2.5981   -0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    5.1962   -0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n    3.8971   -0.7500    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n  3  1  1  0  0  0  0 \n  4  2  1  0  0  0  0 \n  5  3  1  0  0  0  0 \n  6  4  1  0  0  0  0 \n  7  5  1  0  0  0  0 \n  7  6  1  0  0  0  0 \nM  END"
        given().contentType("application/json; charset=UTF-8").when().body(WrappedString(molData)).post("/conversion/mol").`then`().statusCode(200)
      }

      "fail gracefully on bad request" in {
        given().contentType("application/json; charset=UTF-8").when().body(WrappedString("abc")).post("/conversion/smiles").`then`().statusCode(500)

      }
    }
  }
}
