package edu.ucdavis.fiehnlab.mona.backend.curation.reader

import java.io.InputStream

import com.fasterxml.jackson.core.{JsonFactory, JsonToken, JsonParser}
import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.springframework.batch.item.{ItemStream, ItemReader, ExecutionContext}

/**
  * this reader is utilized to efficiently read a large amount of spectra into the system
  */
class JSONFileSpectraReader extends ItemReader[Spectrum] with LazyLogging{

  var stream:InputStream = null

  var parser:JsonParser = null

  val mapper = MonaMapper.create

  /**
    * reads all the data and also takes care of closing/opening the streams
    * @return
    */
  override def read(): Spectrum = {

    if(parser == null){
      logger.debug("opening stream and creating new parser")
      val factory = new JsonFactory
      parser = factory.createParser(stream)
    }

    var token = parser.nextToken()

    if(token == JsonToken.START_ARRAY){
      logger.debug("we have several data spectra in this data set")
      token = parser.nextToken()
    }


    if(token == JsonToken.END_ARRAY){
      logger.debug("read all data, closing stream")
      stream.close()
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

}