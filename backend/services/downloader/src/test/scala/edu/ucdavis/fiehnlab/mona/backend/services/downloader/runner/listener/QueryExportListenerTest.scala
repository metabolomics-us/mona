package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.listener

import java.io.InputStreamReader
import java.util.Date
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryRepository, QueryExportRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.{QueryExport, PredefinedQuery}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.Downloader
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

/**
  * Created by sajjan on 6/9/16.
  */
@SpringBootTest(classes = Array(classOf[Downloader]))
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.downloader", "mona.persistence.init"))
class QueryExportListenerTest extends AnyWordSpec with LazyLogging {

  @Autowired
  val queryExportRepository: QueryExportRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryRepository = null

  @Autowired
  val spectrumResultRepository: SpectrumResultRepository = null

  @Autowired
  val downloadListener: QueryExportListener = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloadListenerTest" must {
    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      spectrumResultRepository.deleteAll()
      exampleRecords.foreach { x =>
        spectrumResultRepository.save(new SpectrumResult(x.getId, x))
      }

      predefinedQueryRepository.deleteAll()
      predefinedQueryRepository.save(new PredefinedQuery("All Spectra", "", "", 0, null, null, null))
    }

    "be able to download a json file using a message" in {
      val jsonExport: QueryExport = new QueryExport("1", "All Spectra", "", "json", null, new Date, 0, 0, null, null)

      downloadListener.handleMessage(jsonExport)

      val result: QueryExport = queryExportRepository.findById(jsonExport.getId).orElse(null)
      assert(result != null)
      assert(result.getCount == 59)
      assert(result.getSize > 0)

      val predefinedQuery: PredefinedQuery = predefinedQueryRepository.findById(result.getLabel).orElse(null)
      assert(predefinedQuery != null)
      assert(predefinedQuery.getJsonExport != null)
      assert(predefinedQuery.getJsonExport.getId == jsonExport.getId)
      assert(predefinedQuery.getMspExport == null)
      assert(predefinedQuery.getQueryCount == 59)
    }


    "be able to download a msp file using a message" in {
      val mspExport: QueryExport = new QueryExport("2", "All Spectra", "", "msp", null, new Date, 0, 0, null, null)

      downloadListener.handleMessage(mspExport)

      val result: QueryExport = queryExportRepository.findById(mspExport.getId).orElse(null)
      assert(result != null)
      assert(result.getCount == 59)
      assert(result.getSize > 0)

      val predefinedQuery: PredefinedQuery = predefinedQueryRepository.findById(result.getLabel).orElse(null)
      assert(predefinedQuery != null)
      assert(predefinedQuery.getMspExport != null)
      assert(predefinedQuery.getMspExport.getId == mspExport.getId)
      assert(predefinedQuery.getQueryCount == 59)
    }


    "be able to download a sdf file using a message" in {
      val sdfExport: QueryExport = new QueryExport("3", "All Spectra", "", "sdf", null, new Date, 0, 0, null, null)

      downloadListener.handleMessage(sdfExport)

      val result: QueryExport = queryExportRepository.findById(sdfExport.getId).orElse(null)
      assert(result != null)
      assert(result.getCount == 59)
      assert(result.getSize > 0)

      val predefinedQuery: PredefinedQuery = predefinedQueryRepository.findById(result.getLabel).orElse(null)
      assert(predefinedQuery != null)
      assert(predefinedQuery.getSdfExport != null)
      assert(predefinedQuery.getSdfExport.getId == sdfExport.getId)
      assert(predefinedQuery.getQueryCount == 59)
    }
  }
}
