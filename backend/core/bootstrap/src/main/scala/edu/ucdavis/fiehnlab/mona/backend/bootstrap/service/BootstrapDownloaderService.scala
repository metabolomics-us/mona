package edu.ucdavis.fiehnlab.mona.backend.bootstrap.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.repository.PredefinedQueryMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.service.DownloadSchedulerService
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.types.PredefinedQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 6/20/16.
  */
@Service
class BootstrapDownloaderService extends LazyLogging {
  @Autowired
  val predefinedQueryRepository: PredefinedQueryMongoRepository = null


  /**
    * Create and save a predefined query object if it does not already exist
    *
    * @param label
    * @param description
    * @param query
    * @return
    */
  def saveQuery(label: String, description: String, query: String) = {
    if (!predefinedQueryRepository.exists(label)) {
      logger.info(s"Saving predefined query: $label")
      predefinedQueryRepository.save(PredefinedQuery(label, description, query, 0, null, null))
    }
  }


  /**
    * Create all predefined queries to become available for download
    *
    * @return
    */
  def createPredefinedQueries() = {
    // Create export for all spectra
    saveQuery("All Spectra", "All Spectra", "")

    // Create library exports
    createLibraryQueries()
  }

  /**
    * Define predefined queries grouped by library
    *
    * @return
    */
  def createLibraryQueries() = {
    saveQuery("Libraries - MassBank", "MassBank Spectral Database", "tags.text==massbank")
    saveQuery("Libraries - ReSpect", "RIKEN MS^n Spectral Database for Phytochemicals", "tags.text==respect")
    saveQuery("Libraries - HMDB", "Human Metabolome Database", "tags.text==hmdb")
    saveQuery("Libraries - GNPS", "Global Natural Product Social Molecular Networking Library", "tags.text==gnps")
    saveQuery("Libraries - LipidBlast", "LipidBlast In-Silico MS/MS Database for Lipid Identification", "tags.text==lipidblast")
    saveQuery("Libraries - FAHFA", "Fatty Acid ester of Hydroxyl Fatty Acid In-Silico Library", "tags.text==FAHFA")
  }
}
