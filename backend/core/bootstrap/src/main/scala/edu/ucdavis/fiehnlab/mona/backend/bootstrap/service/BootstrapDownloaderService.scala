package edu.ucdavis.fiehnlab.mona.backend.bootstrap.service

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.repository.PredefinedQueryMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.PredefinedQuery
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
  def saveQuery(label: String, description: String, query: String): Unit = {
    if (!predefinedQueryRepository.exists(label)) {
      logger.info(s"Saving predefined query: $label")
      predefinedQueryRepository.save(PredefinedQuery(label, description, query, 0, null, null))
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

    saveQuery("Libraries - MassBank", "MassBank Spectral Database", "tags.text==MassBank")
    saveQuery("Libraries - MassBank - MassBank EU", "MassBank EU", """tags.text=="MassBank EU"""")
    saveQuery("Libraries - MassBank - CASMI 2016", "CASMI 2016", """tags.text=="CASMI 2016"""")
    saveQuery("Libraries - ReSpect", "RIKEN MS^n Spectral Database for Phytochemicals", "tags.text==ReSpect")
    saveQuery("Libraries - HMDB", "Human Metabolome Database", "tags.text==HMDB")
    saveQuery("Libraries - GNPS", "Global Natural Product Social Molecular Networking Library", "tags.text==GNPS")
    saveQuery("Libraries - LipidBlast", "LipidBlast In-Silico MS/MS Database for Lipid Identification", "tags.text==LipidBlast")
    saveQuery("Libraries - FAHFA", "Fatty Acid ester of Hydroxyl Fatty Acid In-Silico Library", "tags.text==FAHFA")
    saveQuery("Libraries - iTree", "iTree Mass Spectral Tree Library", "tags.text==iTree")
    saveQuery("Libraries - RTX5 Fiehnlib", "RTX5 Fiehn Lab Metabolic Profiling Library", "tags.text==FiehnLib")
    saveQuery("Libraries - MetaboBASE", "Bruker Sumner MetaboBASE Plant Library", "tags.text==MetaboBASE")
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
    saveQuery("All Spectra - In-Silico Spectra", "In-Silico Spectra", "tags.text=='In-Silico'")
//    saveQuery("All Spectra - Experimental Spectra", "Experimental Spectra", "")

    saveQuery(
      "GC-MS Spectra",
      """tags.text=="GC-MS""""
    )


    saveQuery(
      "LC-MS Spectra",
      """tags.text=="LC-MS""""
    )
    saveQuery(
      "LC-MS Spectra - LC-MS/MS Spectra",
      """tags.text=="LC-MS" and metaData=q='name=="ms level" and value=="MS2"'"""
    )
    saveQuery(
      "LC-MS Spectra - LC-MS/MS Spectra - LC-MS/MS Positive Mode",
      """tags.text=="LC-MS" and metaData=q='name=="ms level" and value=="MS2"' and metaData=q='name=="ionization mode" and value=="positive"'"""
    )
    saveQuery(
      "LC-MS Spectra - LC-MS/MS Spectra - LC-MS/MS Negative Mode",
      """tags.text=="LC-MS" and metaData=q='name=="ms level" and value=="MS2"' and metaData=q='name=="ionization mode" and value=="negative"'"""
    )
  }
}
