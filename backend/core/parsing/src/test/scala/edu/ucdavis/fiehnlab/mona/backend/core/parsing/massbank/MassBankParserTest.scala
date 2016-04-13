package edu.ucdavis.fiehnlab.mona.backend.core.parsing.massbank

import edu.ucdavis.fiehnlab.mona.backend.core.domain._
import org.scalatest._
import scala.io.Source
import scala.util.{Try, Success}

class MassBankParserTest extends WordSpec with Matchers {
  protected val rootPath = "/testRecords"

  "The MassBank record parser" when {
    "given a complete record file" should {
      val src: Source = Source.fromURL(getClass.getResource(s"${rootPath}/singleRecord.txt"))
      val result: Try[Spectrum] = MassBankParser.parse(src)

      "successfully parse the file" in {
        result shouldBe a[Success[_]]

        println(result.get)
      }
    }

//    "given a batch of valid record files" should {
//      val root: Source = Source.fromURL(getClass.getResource(s"${rootPath}/batch"))
//      val results: Iterator[Try[Spectrum]] = root.getLines.map { file =>
//        println(file)
//        val src = Source.fromURL(getClass.getResource(s"${rootPath}/batch/${file}"))
//        MassBankParser.parse(src)
//      }
//
//      "successfully parse all files" in {
//        results.foreach { _ shouldBe a[Success[_]]}
//      }
//    }
  }
}
