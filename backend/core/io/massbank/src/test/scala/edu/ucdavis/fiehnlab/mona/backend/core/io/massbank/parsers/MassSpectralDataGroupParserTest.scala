package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.groups._
import org.scalatest._

class MassSpectralDataGroupParserTest extends WordSpec with Matchers with MassSpectralDataGroupParser {
  "An `MassSpectralDataGroupParser`" should {
    "correctly parse complete base metadata" in {
      val input = """MS$FOCUSED_ION: BASE_PEAK 147
                    |MS$FOCUSED_ION: DERIVATIVE_TYPE 5 TMS
                    |MS$DATA_PROCESSING: WHOLE ChromaTOF ver. 2.32 (Leco)""".stripMargin

      val expected = MassSpectralDataGroup(
        Map(
          "BASE_PEAK" -> "147",
          "DERIVATIVE_TYPE" -> "5 TMS"
        ),
        Map(
          "WHOLE" -> "ChromaTOF ver. 2.32 (Leco)"
        )
      )

      val result = parse(massSpectralDataGroup, input)
      result shouldBe a[Success[_]]
      result.get shouldBe expected
    }
  }

}
