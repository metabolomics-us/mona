package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.groups._
import org.scalatest._

class SampleGroupParserTest extends WordSpec with Matchers with SampleGroupParser {
  "A `SampleGroupParser`" should {

    "correctly parse complete base metadata" in {
      val input =
        """SP$SCIENTIFIC_NAME: Mus musculus
          |SP$NAME: mouse
          |SP$LINEAGE: cellular organisms; Eukaryota; Fungi/Metazoa group; Metazoa; Eumetazoa; Bilateria; Coelomata; Deuterostomia; Chordata; Craniata; Vertebrata; Gnathostomata; Teleostomi; Euteleostomi; Sarcopterygii; Tetrapoda; Amniota; Mammalia; Theria; Eutheria; Euarchontoglires; Glires; Rodentia; Sciurognathi; Muroidea; Muridae; Murinae; Mus
          |SP$LINK: NCBI-TAXONOMY 10090
          |SP$SAMPLE: liver""".stripMargin

      val expected = SampleGroup(
        Some("Mus musculus"),
        Some("cellular organisms; Eukaryota; Fungi/Metazoa group; Metazoa; Eumetazoa; Bilateria; Coelomata; Deuterostomia; Chordata; Craniata; Vertebrata; Gnathostomata; Teleostomi; Euteleostomi; Sarcopterygii; Tetrapoda; Amniota; Mammalia; Theria; Eutheria; Euarchontoglires; Glires; Rodentia; Sciurognathi; Muroidea; Muridae; Murinae; Mus"),
        Map("NCBI-TAXONOMY" -> "10090"),
        Some("liver"),
        Map("SP$NAME" -> List("mouse"))
      )

      val result = parse(sampleGroup, input)
      result shouldBe a[Success[_]]
      result.get shouldBe expected
    }

    "accept iterative lines on the 'SP$LINK' field as a map" in {
      val input =
        """SP$SCIENTIFIC_NAME: Mus musculus
          |SP$LINK: NCBI-TAXONOMY 10090
          |SP$LINK: SAMPLE_DB someId
          |SP$SAMPLE: brain""".stripMargin

      val expected = SampleGroup(
        Some("Mus musculus"),
        None,
        Map(
          "NCBI-TAXONOMY" -> "10090",
          "SAMPLE_DB" -> "someId"
        ),
        Some("brain"),
        Map.empty
      )

      val result = parse(sampleGroup, input)
      result shouldBe a[Success[_]]
      result.get shouldBe expected
    }
  }
}
