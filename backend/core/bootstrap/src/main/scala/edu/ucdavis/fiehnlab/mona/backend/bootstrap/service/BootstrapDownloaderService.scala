package edu.ucdavis.fiehnlab.mona.backend.bootstrap.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.PredefinedQueryRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.PredefinedQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
  * Created by sajjan on 6/20/16.
  */
@Service
class BootstrapDownloaderService extends LazyLogging {

  @Autowired
  val predefinedQueryRepo: PredefinedQueryRepository = null


  /**
    * Create and save a predefined query object if it does not already exist
    *
    * @param label
    * @param description
    * @param query
    * @return
    */
  def saveQuery(label: String, description: String, query: String): Unit = {
    if (!predefinedQueryRepo.existsById(label)) {
      logger.info(s"Saving predefined query: $label")
      predefinedQueryRepo.save(new PredefinedQuery(label, description, query, 0, null, null, null))
    }
  }

  def saveQuery(label: String, query: String): Unit = saveQuery(label, label, query)


  /**
    * Create all predefined queries to become available for download
    *
    * @return
    */
  def createPredefinedQueries(): Unit = {
    // Create library exports
    createLibraryQueries()

    // Create general exports
    createGeneralQueries()
  }

  /**
    * Define predefined queries grouped by library
    *
    * @return
    */
  def createLibraryQueries(): Unit = {
    logger.info("Creating library queries")

    saveQuery("Libraries - MassBank", "MassBank Spectral Database", "tags.text:'MassBank'")
    saveQuery("Libraries - MassBank - CASMI 2012", "CASMI 2012", "tags.text:'CASMI 2012'")
    saveQuery("Libraries - MassBank - CASMI 2016", "CASMI 2016", "tags.text:'CASMI 2016'")
    saveQuery("Libraries - ReSpect", "RIKEN MS^n Spectral Database for Phytochemicals", "tags.text:'ReSpect'")
    saveQuery("Libraries - HMDB", "Human Metabolome Database", "tags.text:'HMDB'")
    saveQuery("Libraries - GNPS", "Global Natural Product Social Molecular Networking Library", "tags.text:'GNPS'")
    saveQuery("Libraries - MetaboBASE", "Bruker Sumner MetaboBASE Plant Library", "tags.text:'MetaboBASE'")
    saveQuery("Libraries - RIKEN IMS Oxidized Phospholipids", "RIKEN IMS Oxidized Phospholipids", "tags.text:'RIKEN OxPLs'")
    saveQuery("Libraries - RTX5 Fiehnlib", "RTX5 Fiehn Lab Metabolic Profiling Library", "tags.text:'FiehnLib'")
    saveQuery("Libraries - Fiehn HILIC", "Fiehn HILIC Library", "tags.text:'Fiehn HILIC'")
    saveQuery("Libraries - Pathogen Box", "Pathogen Box Library", "tags.text:'PathogenBox'")

    saveQuery("Libraries - Vaniya/Fiehn Natural Products Library", "Vaniya/Fiehn Natural Products Library", "tags.text:'VF-NPL'")
    saveQuery("Libraries - Vaniya/Fiehn Natural Products Library - VF-NPL QTOF", "Vaniya/Fiehn Natural Products QTOF Library", "tags.text:'VF-NPL QTOF'")
    saveQuery("Libraries - Vaniya/Fiehn Natural Products Library - VF-NPL QExactive", "Vaniya/Fiehn Natural Products Q Exactive Library", "tags.text:'VF-NPL QExactive'")
    saveQuery("Libraries - Vaniya/Fiehn Natural Products Library - VF-NPL LTQ", "Vaniya/Fiehn Natural Products LTQ Library", "tags.text:'VF-NPL LTQ'")

    saveQuery("Libraries - LipidBlast", "LipidBlast In-Silico MS/MS Database for Lipid Identification", "tags.text:'LipidBlast'")
    saveQuery("Libraries - FAHFA", "Fatty Acid ester of Hydroxyl Fatty Acid In-Silico Library", "tags.text:'FAHFA'")
    saveQuery("Libraries - LipidBlast 2022", "LipidBlast In-Silico MS/MS Database for Lipid Identification", "tags.text:'LipidBlast2022'")
  }

  /**
    * Define predefined queries of general queries
    *
    * @return
    */
  def createGeneralQueries(): Unit = {
    logger.info("Creating general queries")

    // Create export for all spectra
    saveQuery("All Spectra", "All Spectra", "")
    saveQuery("All Spectra - Experimental Spectra", "Experimental Spectra", "not(exists(tags.text in ('In-Silico')))")
    saveQuery("All Spectra - In-Silico Spectra", "In-Silico Spectra", "tags.text:'In-Silico'")

    saveQuery(
      "GC-MS Spectra",
      "tags.text:'GC-MS'"
    )


    saveQuery(
      "LC-MS Spectra",
      "tags.text:'LC-MS'"
    )
    saveQuery(
      "LC-MS Spectra - LC-MS/MS Spectra",
      "tags.text:'LC-MS' and metaData.name:'ms level' and metaData.value:'MS2'"
    )
    saveQuery(
      "LC-MS Spectra - LC-MS/MS Spectra - LC-MS/MS Positive Mode",
      "tags.text:'LC-MS' and exists(metaData.name:'ms level' and metaData.value:'MS2') and exists(metaData.name:'ionization mode' and metaData.value:'positive')"
    )
    saveQuery(
      "LC-MS Spectra - LC-MS/MS Spectra - LC-MS/MS Negative Mode",
      "tags.text:'LC-MS' and exists(metaData.name:'ms level' and metaData.value:'MS2') and exists(metaData.name:'ionization mode' and metaData.value:'negative')"
    )
  }
}
