package edu.ucdavis.fiehnlab.mona.backend.curation.writer

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.client.api.MonaSpectrumRestClient
import org.scalatest.flatspec.AnyFlatSpec
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException

/**
  * Unit tests for the retry behaviour of RestRepositoryWriter. The retry budget is carried per write call, so
  * concurrent consumer threads never share retry state and one spectrum exhausting its retries does not starve
  * the next spectrum
  */
class RestRepositoryWriterRetrySpec extends AnyFlatSpec {

  /**
    * A stub spectrum client that records update calls and fails the configured number of updates with a 5xx
    * before succeeding, standing in for a flaky persistence server
    */
  private class RecordingSpectrumClient extends MonaSpectrumRestClient {
    var failuresRemaining: Int = 0
    var updateCalls: Int = 0

    override def get(id: String): Spectrum = new Spectrum()

    override def updateAsync(dao: Spectrum, id: String): Unit = {
      updateCalls += 1
      if (failuresRemaining > 0) {
        failuresRemaining -= 1
        throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
      }
    }
  }

  /**
    * Writer wired to a stub client with a tiny recovery pause so the tests do not actually wait
    */
  private class TestWriter(client: MonaSpectrumRestClient, retries: Int)
    extends RestRepositoryWriter("test-token", retrySilently = true, maxRetries = retries, recoveryPauseInMS = 1L) {
    override val monaSpectrumRestClient: MonaSpectrumRestClient = client
  }

  private def spectrumWithId(id: String): Spectrum = {
    val spectrum = new Spectrum()
    spectrum.setId(id)
    spectrum
  }

  "RestRepositoryWriter" should "recover when server errors stay within the retry budget" in {
    val client = new RecordingSpectrumClient
    val writer = new TestWriter(client, retries = 3)

    client.failuresRemaining = 2
    writer.write(spectrumWithId("s1"))

    // two failed updates plus the successful third
    assert(client.updateCalls == 3)
  }

  it should "throw once a single spectrum exhausts its retry budget" in {
    val client = new RecordingSpectrumClient
    val writer = new TestWriter(client, retries = 3)

    client.failuresRemaining = 99
    assertThrows[HttpServerErrorException] {
      writer.write(spectrumWithId("s1"))
    }

    // initial attempt plus three retries
    assert(client.updateCalls == 4)
  }

  it should "give each spectrum a fresh retry budget after a prior spectrum exhausted its retries" in {
    val client = new RecordingSpectrumClient
    val writer = new TestWriter(client, retries = 3)

    // First spectrum burns through every retry and fails
    client.failuresRemaining = 99
    assertThrows[HttpServerErrorException] {
      writer.write(spectrumWithId("first"))
    }

    // Second spectrum must still get a full, independent budget and recover. With a shared retry field this
    // would throw immediately, since the previous failure would have left no retries
    client.updateCalls = 0
    client.failuresRemaining = 2
    writer.write(spectrumWithId("second"))

    assert(client.updateCalls == 3)
  }
}
