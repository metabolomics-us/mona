package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire

import java.net.InetAddress
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{CompoundDAO, MetaDataDAO, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.CompoundProcessor
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.client.{HttpStatusCodeException, RestOperations}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, Buffer}
import scala.jdk.CollectionConverters._
/**
  * this class connects to the external classifier processor and computes classification information for this spectra
  */
@Step(description = "run's classification rules from the wishart's lab classyfire tool")
class ClassyfireProcessor extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  @Autowired
  val compoundProcessor: CompoundProcessor = null

  @Autowired
  protected val restOperations: RestOperations = null


  def isReachable: Boolean = {
    try {
      InetAddress.getByName("classyfire.wishartlab.com").isReachable(10)
      true
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  /**
    * build a copy of the provided spectra
    *
    * @param spectrum
    * @return
    */
  override def process(spectrum: Spectrum): Spectrum = {

    if (isReachable) {
      logger.info(s"${spectrum.getId}: Retrieving classification data from ClassyFire")

      val compounds = spectrum.getCompound.asScala.map(compound =>
        if (compound.getClassification == null) {
          compound.setClassification(Buffer[MetaDataDAO]().asJava)
          classify(compound, spectrum.getId)
        } else {
          classify(compound, spectrum.getId)
        }
      )
      spectrum.setCompound(compounds.asJava)
      spectrum
    } else {
      logger.error(s"${spectrum.getId}: ClassyFire is unreachable!")
      spectrum
    }
  }

  /**
    * Call the ClassyFire service and work over it's defined data
    *
    * @param compound
    * @return
    */
  def classify(compound: CompoundDAO, id: String): CompoundDAO = {
    // Look for a ClassyFire query id
    val classyfireQueryId: Buffer[MetaDataDAO] = compound.getClassification.asScala.filter(_.getName == "ClassyFire Query ID")

    // Find provided or calculated InChIKey
    val inchiKey: String =
      if (compound.getMetaData.asScala.exists(x => x.getName == CommonMetaData.INCHI_KEY && x.getComputed)) {
        logger.info(s"Getting computed inchikey")
        compound.getMetaData.asScala.filter(x => x.getName == CommonMetaData.INCHI_KEY && x.getComputed).map(_.getValue.toString).headOption.orNull
      } else {
        logger.info(s"Getting submitted inchikey")
        compound.getInchiKey
      }
    // Retrieve classyfire query
    if (compound.getClassification.size() > classyfireQueryId.length) {
      logger.info(s"$id: Already have ClassyFire data, skipping...")
      compound
    }

    else if (classyfireQueryId.nonEmpty) {
      val url = s"http://classyfire.wishartlab.com/queries/${classyfireQueryId.head.getValue.toString}.json"
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
          logger.warn(s"$id: Received status code ${result.getStatusCode} for query id ${compound.getInchiKey}")
          scheduleClassification(compound, id)
        }
      } catch {
        case x: HttpStatusCodeException =>
          logger.warn(s"$id: Received status code ${x.getStatusCode} for ${compound.getInchiKey}")
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
          logger.warn(s"$id: Received status code ${result.getStatusCode} for ${compound.getInchiKey}")
          scheduleClassification(compound, id)
        }
      } catch {
        case x: HttpStatusCodeException =>
          logger.warn(s"$id: Received status code ${x.getStatusCode} for ${compound.getInchiKey}")
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
  def processClassification(compound: CompoundDAO, id: String, classification: ClassyfireResult, url: String): CompoundDAO = {

    val buffer: ArrayBuffer[MetaDataDAO] = ArrayBuffer()

    if (classification.kingdom != null) {
      val metaData = new MetaDataDAO(null, "kingdom", classification.kingdom.name, false, "classification", true, null)
      buffer += metaData
    }

    if (classification.superclass != null) {
      val metaData = new MetaDataDAO(null, "superclass", classification.superclass.name, false, "classification", true, null)
      buffer += metaData
    }

    if (classification.`class` != null) {
      val metaData = new MetaDataDAO(null, "class", classification.`class`.name, false, "classification", true, null)
      buffer += metaData
    }

    if (classification.subclass != null) {
      val metaData = new MetaDataDAO(null, "subclass", classification.subclass.name, false, "classification", true, null)
      buffer += metaData
    }

    if (classification.intermediate_nodes != null) {
      var level = 1
      classification.intermediate_nodes.foreach { parent =>
        val metaData = new MetaDataDAO(null, s"direct parent level $level", parent.name, false, "classification", true, null)
        buffer += metaData
        level = level + 1
      }
    }

    if (classification.direct_parent != null && classification.direct_parent.name != null) {
      val metaData = new MetaDataDAO(null, "direct parent", classification.direct_parent.name, false, "classification", true, null)
      buffer += metaData
    }

    if (classification.alternative_parents != null) {
      classification.alternative_parents.foreach { parent =>
        val metaData = new MetaDataDAO(null, "alternative parent", parent.name, false, "classification", true, null)
        buffer += metaData
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

    if (compound.getClassification != null) {
      compound.getClassification.asScala.foreach {
        buffer += _
      }
    }

    compound.setClassification(buffer.asJava)
    compound
  }

  /**
    * Schedule the classification of this compound using available structures
    *
    * @param compound
    */
  def scheduleClassification(compound: CompoundDAO, id: String): CompoundDAO = {

    logger.info(s"$id: Scheduling compound classification from structure")

    // Submit InChI or SMILES if available, sorted
    val inchikey: Buffer[MetaDataDAO] = compound.getMetaData.asScala.filter(x => x.getName == CommonMetaData.INCHI_CODE && x.getComputed)
    val smiles: mutable.Buffer[MetaDataDAO] = compound.getMetaData.asScala.filter(x => x.getName == CommonMetaData.SMILES && x.getComputed)

    val structure: String =
      if (inchikey.nonEmpty) inchikey.head.getValue.toString
      else if (smiles.nonEmpty) smiles.head.getValue.toString
      else ""

    if (structure != null) {
      val url = s"http://classyfire.wishartlab.com/queries"
      logger.info(s"$id: Invoking url: $url")

      try {
        val result: ResponseEntity[QueryScheduleResult] = restOperations.postForEntity(url, QueryScheduleRequest("", structure, "STRUCTURE"), classOf[QueryScheduleResult])

        if (result.getStatusCode == HttpStatus.OK) {
          logger.info(s"$id: Scheduled with query id ${result.getBody.id}")
          compound.setClassification(ArrayBuffer[MetaDataDAO](new MetaDataDAO(null, "ClassyFire Query ID", result.getBody.id.toString, true, "none", false, null)).asJava)
          compound
        } else {
          logger.info(s"$id: Received status code ${result.getStatusCode} for $structure")
          compound
        }
      } catch {
        case x: HttpStatusCodeException =>
          logger.warn(s"$id: Received status code ${x.getStatusCode} for ${compound.getInchiKey}")
          compound
      }
    } else {
      logger.info(s"$id: No structure available to submit to ClassyFire")
      compound
    }
  }
}
