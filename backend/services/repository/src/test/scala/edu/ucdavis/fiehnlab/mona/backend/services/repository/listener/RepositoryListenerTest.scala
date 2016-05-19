package edu.ucdavis.fiehnlab.mona.backend.services.repository.listener

import java.io.InputStreamReader

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.event.Event
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.services.repository.Repository
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg_000 on 5/18/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[Repository]))
class RepositoryListenerTest extends WordSpec {

  @Autowired
  val repositoryListener: RepositoryListener = null

  val reader = JSONDomainReader.create[Spectrum]

  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "RepositoryListenerTest" must {

    val input = new InputStreamReader(getClass.getResourceAsStream("/monaRecord.json"))
    val spectrum: Spectrum = reader.read(input)

    "be able to receive data and " should {

      "create a file on an add event" in {
        repositoryListener.received(Event(spectrum,eventType = Event.ADD))
      }

      "create a file on an update event" in {
        repositoryListener.received(Event(spectrum,eventType = Event.UPDATE))
      }

      "delete a file on a delete event" in {
        repositoryListener.received(Event(spectrum,eventType = Event.ADD))
        repositoryListener.received(Event(spectrum,eventType = Event.DELETE))

      }
    }
  }
}
