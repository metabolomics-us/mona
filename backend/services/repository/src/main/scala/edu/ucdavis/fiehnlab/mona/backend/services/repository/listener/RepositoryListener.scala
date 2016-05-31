package edu.ucdavis.fiehnlab.mona.backend.services.repository.listener

import java.io.{File, FileNotFoundException}
import javax.annotation.{PostConstruct, PreDestroy}

import edu.ucdavis.fiehnlab.mona.backend.core.amqp.event.bus.{EventBus, EventBusListener}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.services.repository.layout.FileLayout
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.RefSpec
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

  /**
    * adds a file to the git repository
    * @param file
    * @return
    */
  def gitAdd(file:File) = {
    val path:String = buildPath(file)
    logger.info(s"adding to git ${path}")
    val spectrum:Spectrum = objectMapper.readValue(file,classOf[Spectrum])
    val cache = git.add().addFilepattern(path).call()
    git.commit().setMessage(s"added spectra ${spectrum.id} to the repository").call()
    git.push().setRemote("origin/master").setRefSpecs(new RefSpec("master")).call()
  }

  /**
    * updates the file in the git repository
    * @param file
    * @return
    */
  def gitUpdate(file:File) = {
    val path:String = buildPath(file)

    val spectrum:Spectrum = objectMapper.readValue(file,classOf[Spectrum])
    git.add().setUpdate(true).addFilepattern(path).call()
    git.commit().setMessage(s"updated spectra ${spectrum.id} to the repository").setCommitter(spectrum.submitter.emailAddress,spectrum.submitter.emailAddress).call()
    git.push().setRemote("origin/master").setRefSpecs(new RefSpec("master")).call()
  }

  /**
    * removes a file from the git repository
    * @param file
    * @return
    */
  def gitRemove(file:File) = {
    val filePath = file.getAbsolutePath().substring(layout.baseDir.getAbsolutePath().length+1,file.getAbsolutePath.length)

    logger.info(s"removing file: ${filePath}" )
    val spectrum:Spectrum = objectMapper.readValue(file,classOf[Spectrum])
    git.rm().addFilepattern(filePath).call()
    git.commit().setMessage(s"removed spectra ${spectrum.id} from the repository").setCommitter(spectrum.submitter.emailAddress,spectrum.submitter.emailAddress).call()
    git.push().setRemote("origin/master").setRefSpecs(new RefSpec("master")).call()
  }

  @PreDestroy
  def cleanup = {
    logger.info("closing repository")
    git.close()
  }

  def buildPath(file:File) : String = "."
}