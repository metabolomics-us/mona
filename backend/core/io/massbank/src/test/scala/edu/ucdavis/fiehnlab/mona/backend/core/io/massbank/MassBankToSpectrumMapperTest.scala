package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank

import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import org.scalatest._

import scala.io.Source
import scala.util.{Success, Try}

class MassBankToSpectrumMapperTest extends WordSpec with Matchers {
  protected val rootPath = "/testdata"

  "The MassBank record parser" when {
    "given a complete record file" should {
      val src: Source = Source.fromURL(getClass.getResource(s"$rootPath/singleRecord.txt"))
      val result: Try[Spectrum] = MassBankToSpectrumMapper.parse(src)

      "successfully parse the file" in {
        result shouldBe a[Success[_]]
      }

      "assign ACCESSION as both spectrum ID and as base metadata" in {
        result.get.id shouldBe "PR100162"
        result.get.metaData.filter(m => m.name == "ACCESSION" && m.value == "PR100162") should not be empty
      }

      "format spectrum into string correctly" in {
        result.get.spectrum shouldBe "84.0444:772.6 102.0552:188.1 130.0502:187.7 148.061:86.1"
      }

      "extract CH$INCHI information into the biological compound metadata group" in {
        result.get.compound.head.inchi shouldBe "InChI=1S/C5H9NO4/c6-3(5(9)10)1-2-4(7)8/h3H,1-2,6H2,(H,7,8)(H,9,10)/t3-/m0/s1"
      }

      "extract CH$NAME information into the biological compound metadata group" in {
        val expected = List("L-Glutamic acid",
          "Glu",
          "L-Glutamate",
          "alpha-Glutamic acid",
          "L-alpha-Aminoglutaric Acid",
          "(S)-2-Aminopentanedioic acid",
          "L-1-Aminopropane-1,3-dicarboxylic Acid",
          "1-Aminopropane-1,3-dicarboxylic acid",
          "Aciglut",
          "Glusate",
          "Glutacid",
          "Glutaminol",
          "Glutaton")

        val names = result.get.compound.head.names.map(_.name)
        names shouldBe expected
      }
    }

    "given a batch of valid record files" should {
      val root: Source = Source.fromURL(getClass.getResource(s"$rootPath/batch"))
      val results: Iterator[Try[Spectrum]] = root.getLines.map { file =>
        val src = Source.fromURL(getClass.getResource(s"$rootPath/batch/$file"))
        MassBankToSpectrumMapper.parse(src)
      }

      "successfully parse all files" in {
        results.foreach { result =>
          result shouldBe a[Success[_]]
        }
      }
    }
  }
}
