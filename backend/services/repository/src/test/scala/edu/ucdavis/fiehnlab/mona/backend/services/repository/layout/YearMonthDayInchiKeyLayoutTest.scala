package edu.ucdavis.fiehnlab.mona.backend.services.repository.layout

import java.io.{File, InputStreamReader}
import java.nio.file.Paths
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.test.context.ActiveProfiles

/**
  * Created by wohlg_000 on 5/18/2016.
  */
@ActiveProfiles(Array("test"))
class YearMonthDayInchiKeyLayoutTest extends AnyWordSpec with LazyLogging {

  val reader = JSONDomainReader.create[Spectrum]

  "YearMonthDayInchiKeyLayoutTest" should {
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrum: Spectrum = reader.read(input)

    "layout" in {
      val layout = new YearMonthDayInchiKeyLayout(new File("/"))
      val result = layout.layout(spectrum)

      logger.info(spectrum.getCompound.get(0).getInchiKey)
      logger.info(s"result: ${result}")

      assert(result.getPath.equalsIgnoreCase(Paths.get(s"/2015/8/11/${spectrum.getCompound.get(0).getInchiKey}").toFile.getPath))
    }
  }
}
