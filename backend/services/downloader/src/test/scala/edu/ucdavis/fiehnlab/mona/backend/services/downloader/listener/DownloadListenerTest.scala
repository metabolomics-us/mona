package edu.ucdavis.fiehnlab.mona.backend.services.downloader.listener

import java.io.InputStreamReader

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.{QueryExport, Downloader}
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by sajjan on 6/9/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[Downloader]))
class DownloadListenerTest extends WordSpec with LazyLogging {

  @Autowired
  val mongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val downloadListener: DownloadListener = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloadListenerTest" must {
    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      mongoRepository.deleteAll()

      for (spectrum <- exampleRecords) {
        mongoRepository.save(spectrum)
      }
    }

    "be able to download a file using a message" in {
      val export: QueryExport = QueryExport("All Spectra", "", "json", 0, 0, null, null, null)

      downloadListener.handleMessage(export)
    }
  }
}
