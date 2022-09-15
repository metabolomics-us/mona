package edu.ucdavis.fiehnlab.mona.backend.services.repository.layout

import java.io.{File, InputStreamReader}
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.test.context.ActiveProfiles

/**
  * Created by wohlg_000 on 5/18/2016.
  */
@ActiveProfiles(Array("test"))
class InchiKeyLayoutTest extends AnyWordSpec with LazyLogging {

  val reader = JSONDomainReader.create[Spectrum]

  "InchiKeyLayoutTest" should {
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrum: Spectrum = reader.read(input)

    "layout" in {
      val layout = new InchiKeyLayout(new File("/"))
      val result = layout.layout(spectrum)

      logger.info(spectrum.getCompound.get(0).getInchiKey)

      assert(result.getPath.endsWith(spectrum.getCompound.get(0).getInchiKey))
    }
  }
}
