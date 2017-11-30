package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CompoundProcessor
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.client.{HttpStatusCodeException, RestOperations}

import scala.collection.mutable.ArrayBuffer

/**
  * this class connects to the external classifier processor and computes classification information for this spectra
  */
@Step(description = "run's classification rules from the wishart's lab classyfire tool")
class ClassyfireProcessor extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  @Autowired
  val compoundProcessor: CompoundProcessor = null

  @Autowired
  protected val restOperations: RestOperations = null

  /**
    * build a copy of the provided spectra
    *
    * @param spectrum
    * @return
    */
  override def process(spectrum: Spectrum): Spectrum = {
    logger.info(s"${spectrum.id}: Retrieving classification data from ClassyFire")

    val compounds = spectrum.compound.map(compound =>
      if (compound.classification == null) {
        classify(compound.copy(classification = Array[MetaData]()), spectrum.id)
      } else {
        classify(compound, spectrum.id)
      }
    )

    spectrum.copy(compound = compounds)
  }

  /**
    * Call the ClassyFire service and work over it's defined data
    *
    * @param compound
    * @return
    */
  def classify(compound: Compound, id: String): Compound = {
    // Look for a ClassyFire query id
    val classyfireQueryId: Array[MetaData] = compound.classification.filter(_.name == "ClassyFire Query ID")

    // Find provided or calculated InChIKey
    val inchiKey: String =
      if (compound.inchiKey != null)
        compound.inchiKey
      else
        compound.metaData.filter(_.name == CommonMetaData.INCHI_KEY).map(_.value.toString).headOption.orNull

    // Retrieve classyfire query
    if (compound.classification.length > classyfireQueryId.length) {
      logger.info(s"$id: Already have ClassyFire data, skipping...")
      compound
    }

    else if (classyfireQueryId.nonEmpty) {
      val url = s"http://classyfire.wishartlab.com/queries/${classyfireQueryId.head.value.toString}.json"
      logger.info(s"$id: Invoking url: $url")

      try {
        val result: ResponseEntity[QueryResult] = restOperations.getForEntity(url, classOf[QueryResult])

        if (result.getStatusCode == HttpStatus.OK) {
          val resultBody: QueryResult = result.getBody

          if (resultBody.classification_status == "Done" && resultBody.invalid_entities.isEmpty && resultBody.entities.nonEmpty) {
            logger.info(s"$id: ClassyFire query successful, fetching results")
            processClassification(compound, id, resultBody.entities.head, url)
          } else {
            logger.warn(s"$id: ClassyFire query failed: status = ${resultBody.classification_status}, ${resultBody.entities.length} entities, ${resultBody.invalid_entities.length} invalid entities")
            if (resultBody.invalid_entities.nonEmpty) {
              logger.warn(resultBody.invalid_entities.head.report.mkString("\n"))
            }

            compound
          }
        } else {
          logger.warn(s"$id: Received status code ${result.getStatusCode} for query id ${compound.inchiKey}")
          scheduleClassification(compound, id)
        }
      } catch {
        case x: HttpStatusCodeException =>
          logger.warn(s"$id: Received status code ${x.getStatusCode} for ${compound.inchiKey}")
          scheduleClassification(compound, id)
      }
    }

    // Handle case of no InChIKey
    else if (inchiKey == null) {
      logger.info(s"$id: Unable to call ClassyFire, no InChIKey found")
      scheduleClassification(compound, id)
    }

    // Lookup InChIKey or schedule query as fallback
    else {
      val url = s"http://classyfire.wishartlab.com/entities/$inchiKey.json"
      logger.info(s"$id: Invoking url: $url")

      try {
        val result: ResponseEntity[ClassyfireResult] = restOperations.getForEntity(url, classOf[ClassyfireResult])

        if (result.getStatusCode == HttpStatus.OK) {
          logger.info(s"$id: ClassyFire request successful")
          processClassification(compound, id, result.getBody, url)
        } else {
          logger.warn(s"$id: Received status code ${result.getStatusCode} for ${compound.inchiKey}")
          scheduleClassification(compound, id)
        }
      } catch {
        case x: HttpStatusCodeException =>
          logger.warn(s"$id: Received status code ${x.getStatusCode} for ${compound.inchiKey}")
          scheduleClassification(compound, id)
      }
    }
  }

  /**
    *
    * @param compound
    * @param id
    * @param classification
    * @return
    */
  def processClassification(compound: Compound, id: String, classification: ClassyfireResult, url: String): Compound = {

    val buffer: ArrayBuffer[MetaData] = ArrayBuffer()

    if (classification.kingdom != null)
      buffer += MetaData("classification", computed = true, hidden = false, "kingdom", null, null, null, classification.kingdom.name)

    if (classification.superclass != null)
      buffer += MetaData("classification", computed = true, hidden = false, "superclass", null, null, null, classification.superclass.name)

    if (classification.`class` != null)
      buffer += MetaData("classification", computed = true, hidden = false, "class", null, null, null, classification.`class`.name)

    if (classification.subclass != null)
      buffer += MetaData("classification", computed = true, hidden = false, "subclass", null, null, null, classification.subclass.name)

    if (classification.intermediate_nodes != null) {
      var level = 1
      classification.intermediate_nodes.foreach { parent =>
        buffer += MetaData("classification", computed = true, hidden = false, s"direct parent level $level", null, null, null, parent.name)
        level = level + 1
      }
    }

    if (classification.direct_parent != null && classification.direct_parent.name != null) {
      buffer += MetaData("classification", computed = true, hidden = false, "direct parent", null, null, null, classification.direct_parent.name)
    }

    if (classification.alternative_parents != null) {
      classification.alternative_parents.foreach { parent =>
        buffer += MetaData("classification", computed = true, hidden = false, "alternative parent", null, null, null, parent.name)
      }
    }

    //    Disable LipidMaps terms as they are redundant to the main classification
    //    if (classification.predicted_lipidmaps_terms != null) {
    //      classification.predicted_lipidmaps_terms.foreach { term =>
    //        buffer += MetaData("classification", computed = true, hidden = false, "predicted lipidmaps", null, null, null, term)
    //      }
    //    }

    //    Disable storing of substituents
    //    if (classification.substituents != null) {
    //      classification.substituents.foreach { term =>
    //        buffer += MetaData("classification", computed = true, hidden = false, "substituents", null, null, null, term)
    //      }
    //    }

    if (compound.classification != null) {
      compound.classification.foreach {
        buffer += _
      }
    }

    compound.copy(classification = buffer.toArray)
  }

  /**
    * Schedule the classification of this compound using available structures
    *
    * @param compound
    */
  def scheduleClassification(compound: Compound, id: String): Compound = {

    logger.info(s"$id: Scheduling compound classification from structure")

    // Submit InChI or SMILES if available, sorted
    val inchikey: Array[MetaData] = compound.metaData.filter(_.name == CommonMetaData.INCHI_CODE)
    val smiles: Array[MetaData] = compound.metaData.filter(_.name == CommonMetaData.SMILES)

    val structure: String =
      if (inchikey.nonEmpty) inchikey.head.value.toString
      else if (smiles.nonEmpty) smiles.head.value.toString
      else ""

    if (structure != null) {
      val url = s"http://classyfire.wishartlab.com/queries"
      logger.info(s"$id: Invoking url: $url")

      try {
        val result: ResponseEntity[QueryScheduleResult] = restOperations.postForEntity(url, QueryScheduleRequest("", structure, "STRUCTURE"), classOf[QueryScheduleResult])

        if (result.getStatusCode == HttpStatus.OK) {
          logger.info(s"$id: Scheduled with query id ${result.getBody.id}")
          compound.copy(classification = Array(MetaData("none", computed = false, hidden = true, "ClassyFire Query ID", null, null, null, result.getBody.id)))
        } else {
          logger.info(s"$id: Received status code ${result.getStatusCode} for $structure")
          compound
        }
      } catch {
        case x: HttpStatusCodeException =>
          logger.warn(s"$id: Received status code ${x.getStatusCode} for ${compound.inchiKey}")
          compound
      }
    } else {
      logger.info(s"$id: No structure available to submit to ClassyFire")
      compound
    }
  }
}