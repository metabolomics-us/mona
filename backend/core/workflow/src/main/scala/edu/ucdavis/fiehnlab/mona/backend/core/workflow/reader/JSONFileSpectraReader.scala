package edu.ucdavis.fiehnlab.mona.backend.core.workflow.reader

import java.io.InputStream

import com.fasterxml.jackson.core.{JsonToken, JsonFactory, JsonParser}
import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.batch.item.{ExecutionContext, ItemStream, ItemReader}

/**
  * this reader is utilized to efficiently read a large amount of spectra into the system
  */
class JSONFileSpectraReader extends ItemReader[Spectrum] with ItemStream with LazyLogging{

  var stream:InputStream = null

  var parser:JsonParser = null

  val mapper = MonaMapper.create

  override def read(): Spectrum = {

    var token = parser.nextToken()

    if(token == JsonToken.START_ARRAY){
      logger.debug("we have several data spectra in this data set")
      token = parser.nextToken()
    }


    if(token == JsonToken.END_ARRAY){
      logger.debug("read all data")
      null
    }
    else if(token == null){
      throw new NoSuchElementException("we have no more data in this reader, all spectra have been read already!")
    }
    else{
      val jsonNode: JsonNode = mapper.readTree(parser)
      val spectrum = mapper.treeToValue(jsonNode,classOf[Spectrum])
      logger.trace(s"read: ${spectrum}")
      spectrum
    }
  }

  override def update(executionContext: ExecutionContext): Unit = {


  }

  override def close(): Unit = {
    if(stream != null){
      stream.close()
    }
  }

  override def open(executionContext: ExecutionContext): Unit = {
    val factory = new JsonFactory
    parser = factory.createParser(stream)
  }
}