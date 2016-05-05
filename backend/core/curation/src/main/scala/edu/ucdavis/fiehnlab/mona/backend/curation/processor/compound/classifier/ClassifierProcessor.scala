package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classifier

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Spectrum,MetaData}
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.client.RestTemplate

/**
  * this class connects to the external classifier processor and computes classification information for this spectra
  */
class ClassifierProcessor  extends ItemProcessor[Spectrum, Spectrum] with LazyLogging{

  @Autowired
  val restTemplate:RestTemplate = null

  /**
    * build a copy of the provided spectra
    *
    * @param item
    * @return
    */
  override def process(item: Spectrum): Spectrum = {

    val compounds = item.compound.collect{
      case x:Compound => classify(x)
    }
    item.copy(compound = compounds)
  }

  /**
    * call the classify rest service and work over it's defined data
    *
    * @param compound
    * @return
    */
  def classify(compound:Compound) : Compound = {
    val result:ResponseEntity[Classifier] = restTemplate.getForEntity(s"http://classyfire.wishartlab.com/entities/${compound.inchiKey}.json",classOf[Classifier])

    if(result.getStatusCode == HttpStatus.OK){
      logger.debug("operation was successful, do something now...")
      val classifier:Classifier = result.getBody

      val kingdom = MetaData("classification",true,false,"kindom",null,null,classifier.kingdom.url,classifier.kingdom.name)
      val clazz = MetaData("classification",true,false,"class",null,null,classifier.`class`.url,classifier.`class`.name)
      val subclass = MetaData("classification",true,false,"subclass",null,null,classifier.subclass.url,classifier.subclass.name)
      val superclass  = MetaData("classification",true,false,"superclass",null,null,classifier.superclass.url,classifier.superclass.name)


    }
    else {
      logger.warn(s"received status code ${result.getStatusCode} for inchi ${compound.inchiKey}")
      compound
    }
  }
}