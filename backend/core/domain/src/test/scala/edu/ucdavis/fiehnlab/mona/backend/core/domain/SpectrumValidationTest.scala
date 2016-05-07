package edu.ucdavis.fiehnlab.mona.backend.core.domain

import java.io.InputStreamReader
import javax.validation.Validator

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

import scala.collection.JavaConverters._

/**
  * Created by wohlg_000 on 5/5/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[DomainConfig]))
class SpectrumValidationTest extends WordSpec with LazyLogging{

  @Autowired
  val validator: Validator = null

  val reader = JSONDomainReader.create[Spectrum]

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "SpectrumValidationTest" should {

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrum: Spectrum = reader.read(input)


    "submitter" in {

      val failing = spectrum.copy(submitter = null)

      assert(failing.submitter == null)
      val constraints = validator.validate[Spectrum](failing).asScala

      logger.info(s"${constraints}")


    }

    "spectrum" in {

      val failing = spectrum.copy(spectrum = null)

      val constraints = validator.validate[Spectrum](failing).asScala

      logger.info(s"${constraints}")

    }

    "compound" in {

      val failing = spectrum.copy(compound = Array())

      val constraints = validator.validate[Spectrum](failing).asScala

      logger.info(s"${constraints}")

    }

    "getId" in {

    }

    "authors" in {

    }

    "metaData" in {

    }

    "library" in {

    }

    "score" in {

    }

    "tags" in {

    }

    "splash" in {

    }

    "id" in {

    }

  }
}
