package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestContextManager
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import java.io.InputStreamReader
import javax.validation.Validator
import scala.jdk.CollectionConverters._

/**
  * Created by wohlg_000 on 5/5/2016.
  */
@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SpectrumValidationTest extends AnyWordSpec with LazyLogging {

  @Autowired
  val validator: Validator = null

  val reader: JSONDomainReader[SpectrumResult] = JSONDomainReader.create[SpectrumResult]

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SpectrumValidationTest" should {
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: SpectrumResult = reader.read(input)

    logger.info(s"validator is $validator")


    "read spectrum is valid" in {
      val constraints = validator.validate[SpectrumResult](spectrum).asScala
      logger.info(s"$constraints")

      assert(constraints.isEmpty)
    }

    /*"submitter" in {
      val failing = spectrum.copy(submitter = null)
      assert(failing.submitter == null)

      val constraints = validator.validate[Spectrum](failing).asScala
      logger.info(s"$constraints")

      assert(constraints.size == 1)
    }*/

    /*"spectrum is not null" in {
      val failing = spectrum.copy(spectrum = null)
      assert(failing.spectrum == null)

      val constraints = validator.validate[Spectrum](failing).asScala
      logger.info(s"$constraints")

      assert(constraints.nonEmpty)
    }*/

    /*"spectrum is not empty" in {
      val failing = spectrum.copy(spectrum = "")
      assert(failing.spectrum == "")

      val constraints = validator.validate[Spectrum](failing).asScala
      logger.info(s"$constraints")

      assert(constraints.nonEmpty)
    }*/

    /*"compound must be specified" in {
      val failing = spectrum.copy(compound = Array())
      assert(failing.compound.length == 0)

      val constraints = validator.validate[Spectrum](failing).asScala
      logger.info(s"$constraints")

      assert(constraints.size == 1)
    }*/


    /*"id can be null" in {
      val nonfailing = spectrum.copy(id = null)
      assert(nonfailing.id == null)

      val constraints = validator.validate[Spectrum](nonfailing).asScala
      logger.info(s"$constraints")

      assert(constraints.isEmpty)
    }*/

    /*"id is not empty" in {
      val failing = spectrum.copy(id = "")
      assert(failing.id == "")

      val constraints = validator.validate[Spectrum](failing).asScala
      logger.info(s"$constraints")

      assert(constraints.nonEmpty)
    }*/
/*
    "authors" in {
      val failing = spectrum.copy(authors = null)
      val constraints = validator.validate[Spectrum](failing).asScala

      assert(constraints.isEmpty)
    }

    "metaData" in {
      val failing = spectrum.copy(metaData = null)
      val constraints = validator.validate[Spectrum](failing).asScala

      assert(constraints.isEmpty)
    }

    "splash" in {
      val failing = spectrum.copy(splash = null)
      val constraints = validator.validate[Spectrum](failing).asScala

      assert(constraints.isEmpty)
    }*/
  }
}
