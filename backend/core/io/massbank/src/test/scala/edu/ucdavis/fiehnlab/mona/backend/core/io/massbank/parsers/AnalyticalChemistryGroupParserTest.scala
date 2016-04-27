package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.groups._
import org.scalatest._

class AnalyticalChemistryGroupParserTest extends WordSpec with Matchers with AnalyticalChemistryGroupParser {
  "An `AnalyticalChemistryGroupParser`" should {
    "correctly parse complete base metadata" in {
      val input = """AC$INSTRUMENT: Pegasus III TOF-MS system, Leco; GC 6890, Agilent Technologies
                    |AC$INSTRUMENT_TYPE: GC-EI-TOF
                    |AC$MASS_SPECTROMETRY: MS_TYPE MS
                    |AC$MASS_SPECTROMETRY: ION_MODE POSITIVE
                    |AC$CHROMATOGRAPHY: COLUMN_NAME DB-17MS
                    |AC$CHROMATOGRAPHY: RETENTION_INDEX 1851.4
                    |AC$CHROMATOGRAPHY: RETENTION_TIME 858.163 sec""".stripMargin

      val expected = AnalyticalChemistryGroup(
        Some("Pegasus III TOF-MS system, Leco; GC 6890, Agilent Technologies"),
        Some("GC-EI-TOF"),
        Map(
          "MS_TYPE" -> "MS",
          "ION_MODE" -> "POSITIVE"
        ),
        Map(
          "COLUMN_NAME" -> List("DB-17MS"),
          "RETENTION_INDEX" -> List("1851.4"),
          "RETENTION_TIME" -> List("858.163 sec")
        )
      )

      val result = parse(analyticalChemistryGroup, input)
      result shouldBe a[Success[_]]
      result.get shouldBe expected
      result.get.ionMode shouldBe Some("POSITIVE")
      result.get.msType shouldBe Some("MS")
    }

    "accept iterative lines on the 'SOLVENT' field as a map" in {
      val input = """AC$CHROMATOGRAPHY: SOLVENT A acetonitrile-methanol-water (19:19:2) with 0.1% acetic acid and 0.1% ammonium hydroxide (28%)
                    |AC$CHROMATOGRAPHY: SOLVENT B 2-propanol with 0.1% acetic acid and 0.1% ammonium hydroxide (28%)""".stripMargin

      val expected = AnalyticalChemistryGroup(
        None, None, Map.empty,
        Map(
          "SOLVENT" -> List("A acetonitrile-methanol-water (19:19:2) with 0.1% acetic acid and 0.1% ammonium hydroxide (28%)", "B 2-propanol with 0.1% acetic acid and 0.1% ammonium hydroxide (28%)")
        )
      )

      val result = parse(analyticalChemistryGroup, input)
      result shouldBe a[Success[_]]
      result.get shouldBe expected
    }
  }

}
