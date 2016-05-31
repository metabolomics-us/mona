package edu.ucdavis.fiehnlab.mona.backend.services.repository.layout

import java.io.{File, InputStreamReader}
import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by wohlgemuth on 5/20/16.
  */
class SubmitterInchiKeySplashIdTest extends WordSpec with LazyLogging{
  val reader = JSONDomainReader.create[Spectrum]

  "SubmitterInchiKeySplashIdTest" should {
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrum: Spectrum = reader.read(input)

    "layout" in {
      val layout = new SubmitterInchiKeySplashId(new File("/"))
      val result = layout.layout(spectrum)

      logger.info(spectrum.compound(0).inchiKey)
      logger.info(spectrum.submitter.id)
      logger.info(s"result: ${result}")

      assert(result.getPath.equalsIgnoreCase(Paths.get("/Boise_State_University/QASFUMOKHFSJGL-LAFRSMQTSA-N/splash10-0bt9-0910000000-9c8c58860a0fadd33800").toFile.getPath))
    }
  }
}
