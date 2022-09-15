package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{CompoundDAO, MetaDataDAO, Names, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CalculateCompoundProperties
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.client.RestOperations

import scala.collection.mutable.{ArrayBuffer, Buffer}
import scala.jdk.CollectionConverters._

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
    val updatedCompound: Buffer[CompoundDAO] = spectrum.getCompound.asScala.map(fetchCompoundData)

    // Assembled spectrum with updated compounds
    spectrum.setCompound(updatedCompound.asJava)
    spectrum


  }

  /**
    * Requests compound properties, synonyms and externals ids from the CTS by InChIKey
    *
    * @param compound
    * @return
    */
  def fetchCompoundData(compound: CompoundDAO): CompoundDAO = {
    if (ENABLED) {
      val requestURL: String = URL + compound.getInchiKey

      logger.info(s"Requesting data for compound ${compound.getInchiKey} at url $requestURL")

      val response: ResponseEntity[CTSResponse] = restOperations.getForEntity(requestURL, classOf[CTSResponse])

      if (response.getStatusCode == HttpStatus.OK) {
        logger.debug("Request successful, parsing results...")

        val result: CTSResponse = response.getBody

        // Process names
        val names: Array[Names] = result.synonyms.map(x => new Names(true, x.name, 0.0, "CTS"))

        // ArrayBuffer for metadata
        val metaData: ArrayBuffer[MetaDataDAO] = ArrayBuffer()

        // Process compound properties

        metaData += new MetaDataDAO(null,CommonMetaData.MOLECULAR_FORMULA, result.formula, false, "compound properties", true, null)
        metaData += new MetaDataDAO(null , CommonMetaData.MOLECULAR_WEIGHT, result.molweight.toString, false, "compound properties", true, null)
        metaData += new MetaDataDAO(null, CommonMetaData.TOTAL_EXACT_MASS, result.exactmass.toString, false, "compound properties", true, null)

        // Process external ids
        result.externalIds.foreach { x =>
          metaData += new MetaDataDAO(x.url, x.name, x.value, false, "external ids", true, null)
        }

        val newNames: java.util.List[Names] = compound.getNames
        newNames.addAll(names.toList.asJava)
        compound.setNames(newNames)

        val newMetadata: java.util.List[MetaDataDAO] = compound.getMetaData
        newMetadata.addAll(metaData.asJava)
        compound.setMetaData(newMetadata)

        compound
      }

      else {
        logger.warn(s"Received status code ${response.getStatusCode} for InChIKey ${compound.getInchiKey}")
        compound
      }
    }

    else {
      compound
    }
  }
}
