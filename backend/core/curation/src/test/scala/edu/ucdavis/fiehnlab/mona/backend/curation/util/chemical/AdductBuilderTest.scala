package edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class AdductBuilderTest extends AnyWordSpec with Matchers with LazyLogging {

  "adduct builder" must {
    "find an adduct should handle null" in {
      val result = AdductBuilder.findAdduct(null)
      assert(result._1 == null)
      assert(result._2 == "not found")
    }

    "find a simple positive mode adduct" in {
      val result = AdductBuilder.findAdduct("[M+H]+")
      assert(result._1 == "[M+H]+")
      assert(result._2 == "positive")
    }

    "find a simple negative mode adduct" in {
      val result = AdductBuilder.findAdduct("[M-H]+")
      assert(result._1 == "[M-H]-")
      assert(result._2 == "negative")
    }

    "find an adduct should handle an invalid adduct" in {
      val result = AdductBuilder.findAdduct("M+1")
      assert(result._1 == "M+1")
      assert(result._2 == "not found")
    }
  }
}
