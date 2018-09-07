package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.listener

import java.io.InputStreamReader
import java.util.{Date, Optional}

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
class QueryExportListenerTest extends WordSpec with LazyLogging {

  @Autowired
  val mongoRepository: ISpectrumMongoRepositoryCustom = null

  @Autowired
  val queryExportRepository: QueryExportMongoRepository = null

  @Autowired
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null

  @Autowired
  val downloadListener: QueryExportListener = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "DownloadListenerTest" must {
    // Populate the database
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

    "load some data" in {
      mongoRepository.deleteAll()
      exampleRecords.foreach(mongoRepository.save)

      predefinedQueryRepository.deleteAll()
      predefinedQueryRepository.save(PredefinedQuery("All Spectra", "", "", 0, null, null, null))
    }

    "be able to download a json file using a message" in {
      val jsonExport: QueryExport = QueryExport("1", "All Spectra", "", "json", null, new Date, 0, 0, null, null)

      downloadListener.handleMessage(jsonExport)

      val result: Optional[QueryExport] = queryExportRepository.findById(jsonExport.id)
      assert(result.isPresent)
      assert(result.get().count == 58)
      assert(result.get().size > 0)

      val predefinedQuery: Optional[PredefinedQuery] = predefinedQueryRepository.findById(result.get().label)
      assert(predefinedQuery.isPresent)
      assert(predefinedQuery.get().jsonExport != null)
      assert(predefinedQuery.get().jsonExport.id == jsonExport.id)
      assert(predefinedQuery.get().mspExport == null)
      assert(predefinedQuery.get().queryCount == 58)
    }


    "be able to download a msp file using a message" in {
      val mspExport: QueryExport = QueryExport("2", "All Spectra", "", "msp", null, new Date, 0, 0, null, null)

      downloadListener.handleMessage(mspExport)

      val result: Optional[QueryExport] = queryExportRepository.findById(mspExport.id)
      assert(result.isPresent)
      assert(result.get().count == 58)
      assert(result.get().size > 0)

      val predefinedQuery: Optional[PredefinedQuery] = predefinedQueryRepository.findById(result.get().label)
      assert(predefinedQuery.isPresent)
      assert(predefinedQuery.get().mspExport != null)
      assert(predefinedQuery.get().mspExport.id == mspExport.id)
      assert(predefinedQuery.get().queryCount == 58)
    }


    "be able to download a sdf file using a message" in {
      val sdfExport: QueryExport = QueryExport("2", "All Spectra", "", "sdf", null, new Date, 0, 0, null, null)

      downloadListener.handleMessage(sdfExport)

      val result: Optional[QueryExport] = queryExportRepository.findById(sdfExport.id)
      assert(result.isPresent)
      assert(result.get().count == 58)
      assert(result.get().size > 0)

      val predefinedQuery: Optional[PredefinedQuery] = predefinedQueryRepository.findById(result.get().label)
      assert(predefinedQuery.isPresent)
      assert(predefinedQuery.get().mspExport != null)
      assert(predefinedQuery.get().mspExport.id == sdfExport.id)
      assert(predefinedQuery.get().queryCount == 58)
    }
  }
}
