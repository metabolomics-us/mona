package edu.ucdavis.fiehnlab.mona.backend.core.workflow.reader

import java.io.InputStreamReader

import org.scalatest.WordSpec
import org.springframework.batch.item.ExecutionContext

/**
  * Created by wohlgemuth on 3/18/16.
  */
class JSONFileSpectraReaderTest extends WordSpec {

  "JSONFileSpectraReaderTest" should {

    val reader = new JSONFileSpectraReader

    val ctx = new ExecutionContext()
    "open" in {

      reader.stream = getClass.getResourceAsStream("/monaRecords.json")
      reader.open(ctx)
    }

    "read" in {
      while(true)
      reader.read()
    }


    "close" in {
      reader.close()
    }

  }
}
