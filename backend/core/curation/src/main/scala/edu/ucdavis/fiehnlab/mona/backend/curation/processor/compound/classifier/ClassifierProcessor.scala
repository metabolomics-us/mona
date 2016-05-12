package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classifier

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.client.{HttpClientErrorException, RestOperations, RestTemplate}

import scala.collection.mutable.ArrayBuffer

/**
  * this class connects to the external classifier processor and computes classification information for this spectra
  */
@Step(description = "run's classification rules from the wishart's lab classyfire tool")
class ClassifierProcessor extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  @Autowired
  protected val restOperations: RestOperations = null

  /**
    * build a copy of the provided spectra
    *
    * @param item
    * @return
    */
  override def process(item: Spectrum): Spectrum = {

    val compounds = item.compound.collect {
      case x: Compound => classify(x)
    }
    item.copy(compound = compounds)
  }

  /**
    * call the classify rest service and work over it's defined data
    *
    * @param compound
    * @return
    */
  def classify(compound: Compound): Compound = {
    val url = s"http://classyfire.wishartlab.com/entities/${compound.inchiKey}.json"
    logger.info(s"invoking url $url")
    try {
      val result: ResponseEntity[Classifier] = restOperations.getForEntity(url, classOf[Classifier])

      if (result.getStatusCode == HttpStatus.OK) {
        logger.debug("operation was successful, do something now...")
        val classifier: Classifier = result.getBody

        val buffer: ArrayBuffer[MetaData] = ArrayBuffer()

        if (classifier.kingdom != null)
          buffer += MetaData("classification", computed = true, hidden = false, "kingdom", null, null, "http://classyfire.wishartlab.com/entities/QASFUMOKHFSJGL-LAFRSMQTSA-N", classifier.kingdom.name)

        if (classifier.`class` != null)
          buffer += MetaData("classification", computed = true, hidden = false, "class", null, null, "http://classyfire.wishartlab.com/entities/QASFUMOKHFSJGL-LAFRSMQTSA-N", classifier.`class`.name)

        if (classifier.subclass != null)
          buffer += MetaData("classification", computed = true, hidden = false, "subclass", null, null, "http://classyfire.wishartlab.com/entities/QASFUMOKHFSJGL-LAFRSMQTSA-N", classifier.subclass.name)

        if (classifier.superclass != null)
          buffer += MetaData("classification", computed = true, hidden = false, "superclass", null, null, "http://classyfire.wishartlab.com/entities/QASFUMOKHFSJGL-LAFRSMQTSA-N", classifier.superclass.name)

        if (classifier.alternative_parents != null) {
          classifier.alternative_parents.foreach { parent =>
            buffer += MetaData("classification", computed = true, hidden = false, "alternative parent", null, null, "http://classyfire.wishartlab.com/entities/QASFUMOKHFSJGL-LAFRSMQTSA-N", parent.name)
          }
        }

        if (classifier.intermediate_nodes != null) {
          var level = 0
          classifier.intermediate_nodes.foreach { parent =>
            buffer += MetaData("classification", computed = true, hidden = false, s"direct parent level $level", null, null, "http://classyfire.wishartlab.com/entities/QASFUMOKHFSJGL-LAFRSMQTSA-N", parent.name)
            level = level + 1
          }
        }

        if (classifier.predicted_lipidmaps_terms != null) {
          classifier.predicted_lipidmaps_terms.foreach { term =>
            buffer += MetaData("classification", computed = true, hidden = false, "predicted lipidmaps", null, null, "http://classyfire.wishartlab.com/entities/QASFUMOKHFSJGL-LAFRSMQTSA-N", term)
          }
        }


        if (classifier.substituents != null) {
          classifier.substituents.foreach { term =>
            buffer += MetaData("classification", computed = true, hidden = false, "substituents", null, null, "http://classyfire.wishartlab.com/entities/QASFUMOKHFSJGL-LAFRSMQTSA-N", term)
          }
        }


        buffer += MetaData("classification", computed = true, hidden = false, "direct parent", null, null, "http://classyfire.wishartlab.com/entities/QASFUMOKHFSJGL-LAFRSMQTSA-N", classifier.direct_parent.name)


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
      case x: HttpClientErrorException =>
        logger.warn(s"received status code ${x.getStatusCode} for inchi ${compound.inchiKey}")
        compound
    }
  }
}