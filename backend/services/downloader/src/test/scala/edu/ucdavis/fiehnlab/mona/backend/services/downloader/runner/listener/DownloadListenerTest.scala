package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.listener

import java.io.InputStreamReader
import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISpectrumMongoRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.{PredefinedQueryMongoRepository, QueryExportMongoRepository}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.{PredefinedQuery, QueryExport}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.Downloader
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by sajjan on 6/9/16.
  */
@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[Downloader]))
class DownloadListenerTest extends WordSpec with LazyLogging {

  @Autowired
  val mongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val queryExportRepository: QueryExportMongoRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null

  @Autowired
  val downloadListener: DownloadListener = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloadListenerTest" must {
    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      mongoRepository.deleteAll()
      exampleRecords.foreach(mongoRepository.save(_))

      predefinedQueryRepository.deleteAll()
      predefinedQueryRepository.save(PredefinedQuery("All Spectra", "", "", 0, null, null))
    }

    "be able to download a json file using a message" in {
      val jsonExport: QueryExport = QueryExport("1", "All Spectra", "", "json", null, new Date, 0, 0, null, null)

      downloadListener.handleMessage(jsonExport)

      val result: QueryExport = queryExportRepository.findOne(jsonExport.id)
      assert(result != null)
      assert(result.count == 58)
      assert(result.size > 0)

      val predefinedQuery: PredefinedQuery = predefinedQueryRepository.findOne(result.label)
      assert(predefinedQuery != null)
      assert(predefinedQuery.jsonExport != null)
      assert(predefinedQuery.jsonExport.id == jsonExport.id)
      assert(predefinedQuery.mspExport == null)
      assert(predefinedQuery.queryCount == 58)
    }


    "be able to download a msp file using a message" in {
      val mspExport: QueryExport = QueryExport("2", "All Spectra", "", "msp", null, new Date, 0, 0, null, null)

      downloadListener.handleMessage(mspExport)

      val result: QueryExport = queryExportRepository.findOne(mspExport.id)
      assert(result != null)
      assert(result.count == 58)
      assert(result.size > 0)

      val predefinedQuery: PredefinedQuery = predefinedQueryRepository.findOne(result.label)
      assert(predefinedQuery != null)
      assert(predefinedQuery.mspExport != null)
      assert(predefinedQuery.mspExport.id == mspExport.id)
      assert(predefinedQuery.queryCount == 58)
    }
  }
}
