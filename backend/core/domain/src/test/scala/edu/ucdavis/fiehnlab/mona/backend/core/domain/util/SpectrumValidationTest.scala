package edu.ucdavis.fiehnlab.mona.backend.core.domain.util

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.junit.runner.RunWith
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

import java.io.InputStreamReader
import java.util.Collections
import javax.validation.Validator
import scala.jdk.CollectionConverters._

/**
  * Created by wohlg_000 on 5/5/2016.
  */
@SpringBootTest
@RunWith(classOf[SpringRunner])
@ContextConfiguration(classes = Array(classOf[DomainConfig]))
class SpectrumValidationTest extends AnyWordSpec with LazyLogging {

  @Autowired
  val validator: Validator = null

  val reader: JSONDomainReader[Spectrum] = JSONDomainReader.create[Spectrum]

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SpectrumValidationTest" should {
    val input: InputStreamReader = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: Spectrum = reader.read(input)
    //val spectrum: SpectrumResult = new SpectrumResult(temp.getId, temp)

    logger.info(s"validator is $validator")


    "read spectrum is valid" in {
      val constraints = validator.validate[Spectrum](spectrum).asScala
      logger.info(s"$constraints")

      assert(constraints.isEmpty)
    }

//    "submitter" in {
//      val failing = new Spectrum(spectrum)
//      failing.setSubmitter(null)
//      assert(failing.getSubmitter == null)
//
//      val constraints = validator.validate[Spectrum](failing).asScala
//      logger.info(s"$constraints")
//
//      assert(constraints.size == 1)
//    }

    "spectrum is not null" in {
      val failing = new Spectrum(spectrum)
      failing.setSpectrum(null)
      assert(failing.getSpectrum == null)

      val constraints = validator.validate[Spectrum](failing).asScala
      logger.info(s"$constraints")

      assert(constraints.nonEmpty)
    }

    "spectrum is not empty" in {
      val failing = new Spectrum(spectrum)
      failing.setSpectrum("")
      assert(failing.getSpectrum == "")

      val constraints = validator.validate[Spectrum](failing).asScala
      logger.info(s"$constraints")

      assert(constraints.nonEmpty)
    }

    "compound must be specified" in {
      val failing = new Spectrum(spectrum)
      failing.setCompound(Collections.emptyList())
      assert(failing.getCompound.size() == 0)

      val constraints = validator.validate[Spectrum](failing).asScala
      logger.info(s"$constraints")

      assert(constraints.size == 1)
    }


    "id can be null" in {
      val nonfailing = new Spectrum(spectrum)
      nonfailing.setId(null)
      assert(nonfailing.getId == null)

      val constraints = validator.validate[Spectrum](nonfailing).asScala
      logger.info(s"$constraints")

      assert(constraints.isEmpty)
    }

    "id is not empty" in {
      val failing = new Spectrum(spectrum)
      failing.setId("")
      assert(failing.getId == "")

      val constraints = validator.validate[Spectrum](failing).asScala
      logger.info(s"$constraints")

      assert(constraints.nonEmpty)
    }

    "metaData" in {
      val failing = new Spectrum(spectrum)
      failing.setMetaData(null)
      val constraints = validator.validate[Spectrum](failing).asScala

      assert(constraints.isEmpty)
    }

    "splash" in {
      val failing = new Spectrum(spectrum)
      failing.setSplash(null)
      val constraints = validator.validate[Spectrum](failing).asScala

      assert(constraints.isEmpty)
    }
  }
}
