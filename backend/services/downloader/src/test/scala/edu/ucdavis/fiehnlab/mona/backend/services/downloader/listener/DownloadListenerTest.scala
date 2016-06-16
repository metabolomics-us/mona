package edu.ucdavis.fiehnlab.mona.backend.services.downloader.listener

import java.io.InputStreamReader
import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.Downloader
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.PredefinedQueryMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.{PredefinedQuery, QueryExport}
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
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null

  @Autowired
  val downloadListener: DownloadListener = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloadListenerTest" must {
    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))
    val allSpectra: PredefinedQuery = PredefinedQuery("All Spectra", "", "", 0, null, null)

    "load some data" in {
      mongoRepository.deleteAll()

      for (spectrum <- exampleRecords) {
        mongoRepository.save(spectrum)
      }

      predefinedQueryRepository.deleteAll()
      predefinedQueryRepository.save(allSpectra)
    }

    "be able to download a json file using a message" in {
      val jsonExport: QueryExport = QueryExport("1", "All Spectra", "", "json", null, new Date, 0, 0, null, null)

      downloadListener.handleMessage(jsonExport)
    }
  }
}
