package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank

import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import org.scalatest._

import scala.io.Source
import scala.util.{Success, Try}

class MassBankToSpectrumMapperTest extends WordSpec with Matchers {
  protected val rootPath = "/testdata"

  "The MassBank record parser" when {
    "given a complete record file" should {
      val src: Source = Source.fromURL(getClass.getResource(s"${rootPath}/singleRecord.txt"))
      val result: Try[Spectrum] = MassBankToSpectrumMapper.parse(src)

      "successfully parse the file" in {
        result shouldBe a[Success[_]]

        import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
        println(MonaMapper.create.writeValueAsString(result.get))
        fail("TODO Print out the Spectrum bean to check")
      }

      "format spectrum into string correctly" in {
        result.get.spectrum shouldBe "84.0444:772.6 102.0552:188.1 130.0502:187.7 148.061:86.1"
      }
    }

    "given a batch of valid record files" should {
      val root: Source = Source.fromURL(getClass.getResource(s"${rootPath}/batch"))
      val results: Iterator[Try[Spectrum]] = root.getLines.map { file =>
        val src = Source.fromURL(getClass.getResource(s"${rootPath}/batch/${file}"))
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
