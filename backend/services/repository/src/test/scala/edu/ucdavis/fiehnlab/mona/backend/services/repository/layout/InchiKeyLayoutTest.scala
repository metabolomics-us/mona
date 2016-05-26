package edu.ucdavis.fiehnlab.mona.backend.services.repository.layout

import java.io.InputStreamReader
import java.io.File
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 5/18/2016.
  */
class InchiKeyLayoutTest extends WordSpec with LazyLogging{

  val reader = JSONDomainReader.create[Spectrum]

  "InchiKeyLayoutTest" should {
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrum: Spectrum = reader.read(input)

    "layout" in {
      val layout = new InchiKeyLayout(new File("/"))
      val result = layout.layout(spectrum)

      logger.info(spectrum.compound(0).inchiKey)

      assert(result.getPath.endsWith(spectrum.compound(0).inchiKey))
    }
  }
}
