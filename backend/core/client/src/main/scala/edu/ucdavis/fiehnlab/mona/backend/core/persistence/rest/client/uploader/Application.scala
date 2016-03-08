package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.uploader

import java.io.{File, FileReader}

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.DomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.GenericRestClient
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.config.RestClientConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.{ApplicationArguments, ApplicationRunner, SpringApplication}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.{Import, ComponentScan}
import org.springframework.stereotype.Component
import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 3/7/16.
  */

/**
  * Created by wohlgemuth on 2/29/16.
  */
@ComponentScan
@EnableAutoConfiguration
@Import(Array(classOf[RestClientConfig]))
class Application

object Application {

  def main(args: Array[String]): Unit = {

    val app = new SpringApplication(classOf[Application])
    app.run(args: _*)
  }

}

@Component
class Startup extends ApplicationRunner with LazyLogging{

  @Autowired
  val spectrumRestClient: GenericRestClient[Spectrum, String] = null

  override def run(applicationArguments: ApplicationArguments): Unit = {

    if(applicationArguments.containsOption("file")){

      val reader:DomainReader[Array[Spectrum]] = JSONDomainReader.create[Array[Spectrum]]

      applicationArguments.getOptionValues("file").asScala.foreach { file =>
        logger.info(s"reading spectra:${file} ")

        reader.read(new FileReader(new File(file))).foreach { spectrum =>

          logger.info("adding spectrum...")

          spectrumRestClient.add(spectrum)
        }
      }

    }
    else{
      println("please provide a --file=filename argument to begin uploading these data")
    }
  }
}