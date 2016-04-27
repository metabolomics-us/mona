package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.groups._
import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.types.{ PeakData, PeakTriple }
import org.scalatest._

class MassSpectralPeakDataGroupParserTest extends WordSpec with Matchers with Inside with MassSpectralPeakDataGroupParser {
  "An `MassSpectralPeakDataGroupParser`" should {
    "correctly parse complete base metadata" in {
      val input = """PK$SPLASH: splash10-00ke60z100-113be5f50f91fd032b18
                        |PK$ANNOTATION: 82.9455304521 118.065674262 119.0734992941 165.0664025435 170.0605888841 206.0372666262 229.0061193362 241.0061193362 257.9962829314 274.9990225856 286.9990225856 305.0095872719 323.0201519582
                        |PK$NUM_PEAK: 3
                        |PK$PEAK: m/z int. rel.int.
                        |  646.3223 64380108 999
                        |  647.3252 26819201 416
                        |  648.3309 7305831 113""".stripMargin

      val expected = MassSpectralPeakDataGroup(
        Some("splash10-00ke60z100-113be5f50f91fd032b18"),
        List("82.9455304521 118.065674262 119.0734992941 165.0664025435 170.0605888841 206.0372666262 229.0061193362 241.0061193362 257.9962829314 274.9990225856 286.9990225856 305.0095872719 323.0201519582"),
        3,
        PeakData(List(
          PeakTriple(646.3223, 64380108, 999),
          PeakTriple(647.3252, 26819201, 416),
          PeakTriple(648.3309, 7305831, 113)
        )),
        Map(
          "PK$NUM_PEAK" -> List("3"),
          "PK$PEAK" -> List("m/z int. rel.int.")
        )
      )

      val result = parse(massSpectralPeakDataGroup, input)
      result shouldBe a[Success[_]]
      result.get shouldBe expected
    }

    "accept empty peak values (e.g., when handling merged spectra, 'PK$NUM_PEAK' and 'PK$PEAK' are 'N/A')" in {
      val input = """PK$SPLASH: splash10-00ke60z100-113be5f50f91fd032b18
                            |PK$NUM_PEAK: N/A
                            |PK$PEAK: m/z int. rel.int.
                            |  N/A""".stripMargin
      val expected = MassSpectralPeakDataGroup(
        Some("splash10-00ke60z100-113be5f50f91fd032b18"),
        List(), 0, PeakData.empty,
        Map(
          "PK$NUM_PEAK" -> List("N/A"),
          "PK$PEAK" -> List("m/z int. rel.int.")
        )
      )

      val result = parse(massSpectralPeakDataGroup, input)
      result shouldBe a[Success[_]]
      result.get shouldBe expected
    }

    "accept special 'PK$ANNOTATION' field format (single line)" in {
      val input =
        """PK$ANNOTATION: 82.9455304521 118.065674262 119.0734992941 165.0664025435 170.0605888841 206.0372666262 229.0061193362 241.0061193362 257.9962829314 274.9990225856 286.9990225856 305.0095872719 323.0201519582
                    |PK$NUM_PEAK: N/A
                    |PK$PEAK: m/z int. rel.int.
                    |  N/A""".stripMargin

      val expected = MassSpectralPeakDataGroup(
        None,
        List("82.9455304521 118.065674262 119.0734992941 165.0664025435 170.0605888841 206.0372666262 229.0061193362 241.0061193362 257.9962829314 274.9990225856 286.9990225856 305.0095872719 323.0201519582"),
        0, PeakData(List.empty),
        Map(
          "PK$NUM_PEAK" -> List("N/A"),
          "PK$PEAK" -> List(
            "m/z int. rel.int."
          )
        )
      )

      inside(parse(massSpectralPeakDataGroup, input)) {
        case Success(value, _) => value shouldBe expected
      }
    }

    "accept special 'PK$ANNOTATION' field format (multiple line)" in {

      val input = """PK$ANNOTATION: m/z num ( type mass error(ppm) formula )
                     |  687.38 1
                     |    [SM(18:1,16:0)-CH3]- 687.5440996926 -238 C38H76N2O6P-
                     |PK$NUM_PEAK: N/A
                     |PK$PEAK: m/z int. rel.int.
                     |  N/A""".stripMargin

      val expected = MassSpectralPeakDataGroup(
        None,
        List("m/z num ( type mass error(ppm) formula )", "687.38 1", "[SM(18:1,16:0)-CH3]- 687.5440996926 -238 C38H76N2O6P-"),
        0, PeakData(List.empty),
        Map(
          "PK$NUM_PEAK" -> List("N/A"),
          "PK$PEAK" -> List("m/z int. rel.int.")
        )
      )

      inside(parse(massSpectralPeakDataGroup, input)) {
        case Success(value, _) => value shouldBe expected
      }
    }

    "reject peaks when `NUM_PEAK` is different from number of parsed peaks" in {
      val input = """PK$NUM_PEAK: 2
                            |PK$PEAK: m/z int. rel.int.
                            |  646.3223 64380108 999
                            |  647.3252 26819201 416
                            |  648.3309 7305831 113""".stripMargin

      intercept[IllegalArgumentException](parse(massSpectralPeakDataGroup, input))
    }
  }

}
