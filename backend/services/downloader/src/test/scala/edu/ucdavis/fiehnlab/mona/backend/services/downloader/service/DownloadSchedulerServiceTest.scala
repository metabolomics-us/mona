package edu.ucdavis.fiehnlab.mona.backend.services.downloader.service

import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.Downloader
import org.junit.runner.RunWith
import org.scalatest.concurrent.Eventually
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 5/26/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[Downloader]))
class DownloadSchedulerServiceTest extends AbstractSpringControllerTest with Eventually {

  @Autowired
  val downloadSchedulerService: DownloadSchedulerService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloadSchedulerServiceTest" should {
    "scheduleDownload" in {
      assert(1 == 1)
    }
  }
}