package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.chemspider

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestOperations

/**
  * Created by sajjan on 4/6/17.
  */
@Step(description = "this step fetches all compound data ChemSpider", workflow = "spectra-curation")
class FetchChemSpiderCompoundData extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  private val TOKEN: String = "15bdfcf3-d5f1-459d-b97a-4e3b43a9a445"

  @Autowired
  protected val restOperations: RestOperations = null


  override def process(spectrum: Spectrum): Spectrum = {
    val updatedCompound: Array[Compound] = spectrum.compound.map(compound => fetchCompoundData(compound, spectrum.id))

    // Assembled spectrum with updated compounds
    spectrum.copy(compound = updatedCompound)
  }

  /**
    * Requests compound properties from ChemSpider
    *
    * @param compound
    * @return
    */
  def fetchCompoundData(compound: Compound, id: String): Compound = {
    compound
  }

  private def simpleSearch(inchiKey: String, id: String): String = {
    val requestURL = s"http://parts.chemspider.com/JSON.ashx?op=SimpleSearch&searchOptions.QueryText=$inchiKey"
    logger.info(s"$id: Invoking url: $requestURL")

    val result: ResponseEntity[String] = restOperations.getForEntity(requestURL, classOf[String])

    result.getBody
  }
}
