package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import java.io.InputStreamReader
import java.nio.file.{Paths, Files}
import javax.annotation.PostConstruct

import com.jayway.restassured.RestAssured
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.ReceivedEventCounter
import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.config.Notification
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.QueryDownloader
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.QueryExport
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.amqp.core.{Message, MessageListener, Queue}
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.beans.factory.annotation.{Value, Autowired}
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.stereotype.Component
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.collection.JavaConverters._
import scala.concurrent.duration._

/**
  * Created by sajjan on 6/2/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[QueryDownloader]))//,classOf[RestClientTestConfig]))
class QueryDownloaderServiceTest extends AbstractSpringControllerTest with Eventually {

  @Autowired
  val queryDownloaderService: QueryDownloaderService = null

//  @Autowired
//  val restClient:MonaSpectrumRestClient = null

  @Value("${mona.export.path:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_exports")
  val dir: String = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "QueryDownloaderServiceTest" should {
    RestAssured.baseURI = s"http://localhost:$port/rest"

    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))





    val export: QueryExport = QueryExport("test", "", "json", 0, 0, null)
    queryDownloaderService.download(export)

    "export the query file" in {
      assert(Files.exists(Paths.get(dir, "test-query.txt")))
      assert(new String(Files.readAllBytes(Paths.get(dir, "test-query.txt"))).equals(export.query))
    }
  }
}