package edu.ucdavis.fiehnlab.mona.backend.services.repository.layout

import java.io.{File, InputStreamReader}
import java.nio.file.Paths

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec

/**
  * Created by wohlg_000 on 5/18/2016.
  */
class YearMonthDayInchiKeyLayoutTest extends WordSpec with LazyLogging{

  val reader = JSONDomainReader.create[Spectrum]

  "YearMonthDayInchiKeyLayoutTest" should {
    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))

    val spectrum: Spectrum = reader.read(input)


    "layout" in {

      val layout = new YearMonthDayInchiKeyLayout(new File("/"))

      val result = layout.layout(spectrum)

      logger.info(spectrum.compound(0).inchiKey)
      logger.info(s"result: ${result}")

      assert(result.getPath.equalsIgnoreCase(Paths.get(s"/2015/8/11/${spectrum.compound(0).inchiKey}").toFile.getPath))
    }
  }
}
