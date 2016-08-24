package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{MetaData, Names, Compound, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CalculateCompoundProperties
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classifier.Classifier
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.client.RestOperations

import scala.collection.mutable.ArrayBuffer

/**
  * Created by wohlgemuth on 3/14/16.
  */
@Step(description = "this step fetches all external compounds id's from the CTS system", previousClass = classOf[CalculateCompoundProperties], workflow = "spectra-curation")
class FetchCTSCompoundData extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {
  val ENABLED: Boolean = true

  val URL: String = "http://cts.fiehnlab.ucdavis.edu/service/compound/"

  @Autowired
  protected val restOperations: RestOperations = null

  override def process(spectrum: Spectrum): Spectrum = {
    val updatedCompound: Array[Compound] = spectrum.compound.map(fetchCompoundData)

    // Assembled spectrum with updated compounds
    spectrum.copy(compound = updatedCompound)
  }

  /**
    * Requests compound properties, synonyms and externals ids from the CTS by InChIKey
    *
    * @param compound
    * @return
    */
  def fetchCompoundData(compound: Compound): Compound = {
    if (ENABLED) {
      val requestURL: String = URL + compound.inchiKey

      logger.info(s"Requesting data for compound ${compound.inchiKey} at url $requestURL")

      val url = s"http://classyfire.wishartlab.com/entities/${compound.inchiKey}.json"
      logger.info(s"invoking url ${url}")

      val response: ResponseEntity[CTSResponse] = restOperations.getForEntity(requestURL, classOf[CTSResponse])

      if (response.getStatusCode == HttpStatus.OK) {
        logger.debug("Request successful, parsing results...")

        val result: CTSResponse = response.getBody

        // Process names
        val names: Array[Names] = result.synonyms.map(x => Names(computed = true, x.name, 0.0, "CTS"))

        // ArrayBuffer for metadata
        val metaData: ArrayBuffer[MetaData] = ArrayBuffer()

        // Process compound properties
        metaData += MetaData("compound properties", computed = true, hidden = false, CommonMetaData.MOLECULAR_FORMULA, null, null, null, result.formula)
        metaData += MetaData("compound properties", computed = true, hidden = false, CommonMetaData.MOLECULAR_WEIGHT, null, null, null, result.molweight)
        metaData += MetaData("compound properties", computed = true, hidden = false, CommonMetaData.TOTAL_EXACT_MASS, null, null, null, result.exactmass)

        // Process external ids
        result.externalIds.foreach { x =>
          metaData += MetaData("external ids", computed = true, hidden = false, x.name, null, null, x.url, x.value)
        }

        compound.copy(
          names = compound.names ++ names,
          metaData = compound.metaData ++ metaData
        )
      }

      else {
        logger.warn(s"Received status code ${response.getStatusCode} for InChIKey ${compound.inchiKey}")
        compound
      }
    }

    else {
      compound
    }
  }
}