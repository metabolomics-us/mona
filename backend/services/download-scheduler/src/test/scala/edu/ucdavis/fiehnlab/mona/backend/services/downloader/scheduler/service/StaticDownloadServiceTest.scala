package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.AbstractSpringControllerTest
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.StaticDownload
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.DownloadScheduler
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[DownloadScheduler]), webEnvironment = WebEnvironment.DEFINED_PORT)
class StaticDownloadServiceTest extends AbstractSpringControllerTest with LazyLogging {

  @Autowired
  val staticDownloadService: StaticDownloadService = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "StaticDownloadServiceTest" should {
    "recursively delete static download directory" in {
      staticDownloadService.removeStaticDownloadDirectory()
    }

    "return an empty array when there are no static downloads" in {
      assert(staticDownloadService.listStaticDownloads().length == 0)
    }

    "upload a static file with no category" in {
      // Build file to upload and store it
      val file: MockMultipartFile = new MockMultipartFile("monaRecord.json", getClass.getResourceAsStream("/monaRecord.json"))

      staticDownloadService.storeStaticFile(file)

      // Verify that it appears in the list of static downloads
      val fileList: Array[StaticDownload] = staticDownloadService.listStaticDownloads()

      assert(fileList.length == 1)
      assert(fileList.map(_.fileName).last == "monaRecord.json")
      staticDownloadService.fileExists("monaRecord.json")
    }

    "upload a the same static file with no category to ensure that files can be overwritten" in {
      // Build file to upload and store it
      val file: MockMultipartFile = new MockMultipartFile("monaRecord.json", getClass.getResourceAsStream("/monaRecord.json"))

      staticDownloadService.storeStaticFile(file, description = "test")

      // Verify that it appears in the list of static downloads
      val fileList: Array[StaticDownload] = staticDownloadService.listStaticDownloads()

      assert(fileList.length == 1)
      assert(fileList.map(_.fileName).last == "monaRecord.json")
      staticDownloadService.fileExists("monaRecord.json")
      staticDownloadService.fileExists("monaRecord.json.description.txt")
    }

    "upload a static file with a test category" in {
      // Build file to upload and store it
      val file: MockMultipartFile = new MockMultipartFile("gcmsRecord.json", getClass.getResourceAsStream("/gcmsRecord.json"))

      staticDownloadService.storeStaticFile(file, category = "test")

      // Verify that it appears in the list of static downloads
      val fileList: Array[StaticDownload] = staticDownloadService.listStaticDownloads()

      assert(fileList.length == 2)
      assert(fileList.map(_.fileName).contains("gcmsRecord.json"))
      staticDownloadService.fileExists("gcmsRecord.json")
    }
  }
}
