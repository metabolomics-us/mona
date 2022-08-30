package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.config

import java.util
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.io.DomainWriter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.io.msp.MSPWriter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.io.png.PNGWriter
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.io.sdf.SDFWriter
import org.springframework.http.{HttpInputMessage, HttpOutputMessage, MediaType}
import org.springframework.http.converter.AbstractHttpMessageConverter

import scala.jdk.CollectionConverters._
import scala.collection.AbstractIterable
import scala.collection.mutable.AbstractBuffer

/**
  * Generic message converter for MoNA types
  */
class RestMessageConverter(writer: DomainWriter, mimeType: String) extends AbstractHttpMessageConverter[Any](MediaType.valueOf(mimeType)) {

  override def readInternal(clazz: Class[_ <: Any], inputMessage: HttpInputMessage): SpectrumResult = throw new RuntimeException("read is not supported!")

  override def canWrite(mediaType: MediaType): Boolean = {
    mediaType != null && mediaType.equals(MediaType.valueOf(mimeType))
  }

  override def canRead(mediaType: MediaType): Boolean = false

  override def writeInternal(t: Any, outputMessage: HttpOutputMessage): Unit = {
    def write(x: Any): Unit = writer.write(x.asInstanceOf[SpectrumResult], outputMessage.getBody)
    t match {
      case y: SpectrumResult => write(y)
      case x: Iterable[_] => x.foreach(write)
      case x: util.Collection[_] => x.asScala.foreach(write)
      case _ => logger.error(s"Undetermined type $t")
    }
  }

  override def supports(clazz: Class[_]): Boolean = {
    clazz match {
      case q if q.isInstanceOf[Class[SpectrumResult]] => true
      case q if q.isInstanceOf[Class[Iterable[_]]] => true
      case q if q.isInstanceOf[Class[AbstractIterable[_]]] => true
      case q if q.isInstanceOf[Class[AbstractBuffer[_]]] => true
      case q if q.isInstanceOf[Class[util.Collection[_]]] => true
      case _ =>
        logger.debug(s"Unknown class type provided to $mimeType converter: $clazz")
        false
    }
  }
}


/**
  * Converts JSON records to MSP
  */
class MSPConverter extends RestMessageConverter(new MSPWriter, "text/msp")

/**
  * Converts JSON records to SDF
  */
class SDFConverter extends RestMessageConverter(new SDFWriter, "text/sdf")

/**
  * Converts JSON records to SDF
  */
class PNGConverter extends RestMessageConverter(new PNGWriter, "image/png")
