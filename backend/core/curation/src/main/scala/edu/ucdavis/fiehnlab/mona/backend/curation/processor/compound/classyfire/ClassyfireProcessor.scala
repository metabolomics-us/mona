package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
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
  protected val restOperations: RestOperations = null

  /**
    * build a copy of the provided spectra
    *
    * @param spectrum
    * @return
    */
  override def process(spectrum: Spectrum): Spectrum = {
    logger.info(s"${spectrum.id}: Retrieving classification data from ClassyFire")

    val compounds = spectrum.compound.map(classify)
    spectrum.copy(compound = compounds)
  }

  /**
    * Call the ClassyFire service and work over it's defined data
    *
    * @param compound
    * @return
    */
  def classify(compound: Compound): Compound = {
    // Find provided or calculated InChIKey
    val inchiKey: String =
      if (compound.inchiKey != null)
        compound.inchiKey
      else
        compound.metaData.filter(_.name == CommonMetaData.INCHI_KEY).map(_.value.toString).headOption.orNull


    if (inchiKey == null) {
      logger.warn("Unable to call ClassyFire: no InChIKey")
      compound
    }

    else {
      val url = s"http://classyfire.wishartlab.com/entities/$inchiKey.json"
      logger.info(s"Invoking url: $url")

      try {
        val result: ResponseEntity[Classifier] = restOperations.getForEntity(url, classOf[Classifier])

        if (result.getStatusCode == HttpStatus.OK) {
          logger.debug("ClassyFire request successful")

          val classifier: Classifier = result.getBody

          val buffer: ArrayBuffer[MetaData] = ArrayBuffer()

          if (classifier.kingdom != null)
            buffer += MetaData("classification", computed = true, hidden = false, "kingdom", null, null, url, classifier.kingdom.name)

          if (classifier.`class` != null)
            buffer += MetaData("classification", computed = true, hidden = false, "class", null, null, url, classifier.`class`.name)

          if (classifier.subclass != null)
            buffer += MetaData("classification", computed = true, hidden = false, "subclass", null, null, url, classifier.subclass.name)

          if (classifier.superclass != null)
            buffer += MetaData("classification", computed = true, hidden = false, "superclass", null, null, url, classifier.superclass.name)

          if (classifier.alternative_parents != null) {
            classifier.alternative_parents.foreach { parent =>
              buffer += MetaData("classification", computed = true, hidden = false, "alternative parent", null, null, url, parent.name)
            }
          }

          if (classifier.intermediate_nodes != null) {
            var level = 0
            classifier.intermediate_nodes.foreach { parent =>
              buffer += MetaData("classification", computed = true, hidden = false, s"direct parent level $level", null, null, url, parent.name)
              level = level + 1
            }
          }

          if (classifier.predicted_lipidmaps_terms != null) {
            classifier.predicted_lipidmaps_terms.foreach { term =>
              buffer += MetaData("classification", computed = true, hidden = false, "predicted lipidmaps", null, null, url, term)
            }
          }

          if (classifier.substituents != null) {
            classifier.substituents.foreach { term =>
              buffer += MetaData("classification", computed = true, hidden = false, "substituents", null, null, url, term)
            }
          }

          if (classifier.direct_parent != null && classifier.direct_parent.name != null) {
            buffer += MetaData("classification", computed = true, hidden = false, "direct parent", null, null, url, classifier.direct_parent.name)
          }

          if (compound.classification != null) {
            compound.classification.foreach {
              buffer += _
            }
          }

          compound.copy(classification = buffer.toArray)
        }

        else {
          logger.warn(s"received status code ${result.getStatusCode} for inchi ${compound.inchiKey}")
          compound
        }
      }

      catch {
        case x: HttpStatusCodeException =>
          logger.warn(s"received status code ${x.getStatusCode} for inchi ${compound.inchiKey}")
          compound
      }
    }
  }
}