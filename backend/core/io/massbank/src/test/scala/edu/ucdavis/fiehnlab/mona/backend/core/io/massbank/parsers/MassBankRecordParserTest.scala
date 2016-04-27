package edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.parsers

import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.{ MassBankRecordParsingException, MassBankRecordReader }
import edu.ucdavis.fiehnlab.mona.backend.core.io.massbank.types.MassBankRecord
import org.scalatest._

import scala.io.Source
import scala.util.Try

class MassBankRecordParserTest extends WordSpec with Matchers with MassBankRecordParser with Inside {
  protected val rootPath = "/testdata"

  "A `MassBankRecordParser`" should {

    "correctly parse sample data" in {
      val sources = Source.fromURL(getClass.getResource(s"${rootPath}/batch"))
      sources should not be null

      def parseFiles(filename: String): Try[MassBankRecord] = {
        val source = Source.fromURL(getClass.getResource(s"${rootPath}/batch/" + filename))
        source should not be null
        MassBankRecordReader.read(source)
      }

      val filenames = sources.getLines
      filenames.map(parseFiles).foreach(_ shouldBe a[util.Success[_]])
    }

    // TODO Must write more tests to simulate failure cases, this test is not enough for coverage
    "reject empty input" in {
      val result = MassBankRecordReader.read("")
      result shouldBe a[util.Failure[_]]
      intercept[MassBankRecordParsingException] { result.get }
    }

  }
}
