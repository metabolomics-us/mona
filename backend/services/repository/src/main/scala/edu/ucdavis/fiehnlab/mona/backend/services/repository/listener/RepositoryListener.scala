package edu.ucdavis.fiehnlab.mona.backend.services.repository.listener

import java.io.{File, FileNotFoundException}

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, EventBusListener}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.services.repository.layout.FileLayout
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{ControllerAdvice, ExceptionHandler, ResponseStatus}

/**
  * Created by wohlg_000 on 5/18/2016.
  */
class RepositoryListener @Autowired()(val bus: EventBus[Spectrum], val layout: FileLayout, val git:Git) extends EventBusListener[Spectrum](bus) {

  /**
    * an element has been received from the bus and should be now processed
    *
    * @param event
    */
  override def received(event: Event[Spectrum]): Unit = {

    val dir = layout.layout(event.content)

    if(!dir.exists()){
      dir.mkdirs()
    }

    val file = new File(layout.layout(event.content), s"${event.content.id}.json")

    logger.info(s"file is: ${file}")
    event.eventType match {
      //we only care about ADDs at this point in time
      case Event.ADD =>
        //writes the spectra to the while
        logger.info(s"\twriting spectrum with id ${event.content.id}")
        objectMapper.writeValue(file, event.content)
        gitAdd(file)
      case Event.UPDATE =>
        //writes the spectra to the while
        logger.info(s"\twriting spectrum with id ${event.content.id}")
        objectMapper.writeValue(file, event.content)
        gitUpdate(file)

      case Event.DELETE =>
        if (file.exists()) {
          logger.info(s"\tdeleted spectrum with id ${event.content.id}")
          gitRemove(file)
          file.delete()
        }
      case _ => //ignore not of interest
    }
  }

  def gitAdd(file:File) = {
    val spectrum:Spectrum = objectMapper.readValue(file,classOf[Spectrum])
    git.add().addFilepattern(file.getAbsolutePath).call()
    git.commit().setMessage(s"added spectra ${spectrum.id} to the repository").setAll(true).setCommitter(spectrum.submitter.emailAddress,spectrum.submitter.emailAddress).call()
  }

  def gitUpdate(file:File) = {
    val spectrum:Spectrum = objectMapper.readValue(file,classOf[Spectrum])
    git.add().addFilepattern(file.getAbsolutePath).call()
    git.commit().setMessage(s"updated spectra ${spectrum.id} to the repository").setAll(true).setCommitter(spectrum.submitter.emailAddress,spectrum.submitter.emailAddress).call()
  }

  def gitRemove(file:File) = {
    val spectrum:Spectrum = objectMapper.readValue(file,classOf[Spectrum])
    git.rm().addFilepattern(file.getAbsolutePath).call()
    git.commit().setMessage(s"removed spectra ${spectrum.id} from the repository").setAll(true).setCommitter(spectrum.submitter.emailAddress,spectrum.submitter.emailAddress).call()
  }
}