package edu.ucdavis.fiehnlab.mona.backend.core.curation.runner

import java.io.InputStreamReader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.concurrent.Eventually
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{Bean, Configuration, Primary}
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

import scala.concurrent.duration._
import scala.language.postfixOps


/**
  * Created by sajjan on 5/31/2017.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[CurationRunner], classOf[TestConfig]))
class CurationRunnerTest extends WordSpec with Eventually with LazyLogging {

  @Autowired
  val curationListener: TestCurationListener = null

  @Autowired
  @Qualifier("spectra-curation-queue")
  val queueName: String = null

  @Autowired
  val rabbitTemplate: RabbitTemplate = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "CurationRunnerTest" should {
    val exampleSpectra: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "manually handle many spectra" in {
      curationListener.asInstanceOf[TestCurationListener].resetMessageCount()
      exampleSpectra.foreach(curationListener.handleMessage)
      assert(curationListener.asInstanceOf[TestCurationListener].messageCount == exampleSpectra.length)
    }

    "handle a many spectrum via rabbitmq" in {
      curationListener.asInstanceOf[TestCurationListener].resetMessageCount()
      exampleSpectra.foreach(spectrum => rabbitTemplate.convertAndSend(queueName, spectrum))

      eventually(timeout(10 seconds)) {
        assert(curationListener.asInstanceOf[TestCurationListener].messageCount == exampleSpectra.length)
      }
    }
  }
}

class TestCurationListener extends CurationListener(null, null) with LazyLogging {
  var messageCount: Int = 0

  override def handleMessage(spectra: Spectrum): Unit = {
    logger.info(s"Received spectrum: ${spectra.id}")
    messageCount += 1
  }

  def resetMessageCount(): Unit = messageCount = 0
}

@Configuration
class TestConfig {

  @Bean
  @Primary
  def curationListener: CurationListener = new TestCurationListener()
}