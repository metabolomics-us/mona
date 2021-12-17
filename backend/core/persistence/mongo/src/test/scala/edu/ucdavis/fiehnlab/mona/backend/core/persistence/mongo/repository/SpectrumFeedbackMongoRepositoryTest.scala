package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository
import edu.ucdavis.fiehnlab.mona.backend.core.domain.SpectrumFeedback
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.MongoConfig
import org.junit.runner.RunWith
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.elasticsearch.{ElasticsearchAutoConfiguration, ElasticsearchDataAutoConfiguration}
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}

import scala.jdk.CollectionConverters._

@RunWith(classOf[SpringRunner])
@ContextConfiguration(classes = Array(classOf[SecondConfig]))
@TestPropertySource(locations = Array("classpath:application.properties"))
class SpectrumFeedbackMongoRepositoryTest extends AnyWordSpec {

  @Autowired
  val spectrumFeedbackMongoRepository: SpectrumFeedbackMongoRepository = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "SpectrumFeedbackMongoRepositoryTest" should {
    "delete all data " in {
      spectrumFeedbackMongoRepository.deleteAll()
      assert(spectrumFeedbackMongoRepository.count() == 0)
    }

    "add feedback" in {
      spectrumFeedbackMongoRepository.save(SpectrumFeedback(null, "Test123", "test@gmail.com", "spectrum_quality", "noisy"))
    }

    "findByMonaId" in {
      assert(spectrumFeedbackMongoRepository.findByMonaID("Test123").asScala.size == 1)
    }

    "add more feedback" in {
      spectrumFeedbackMongoRepository.save(SpectrumFeedback(null, "Test123", "test@gmail.com", "spectrum_quality", "grassy"))
      spectrumFeedbackMongoRepository.save(SpectrumFeedback(null, "Test124", "test2@gmail.com", "spectrum_quality", "noisy"))
    }

    "findByMonaID returns 2" in {
      assert(spectrumFeedbackMongoRepository.findByMonaID("Test123").asScala.size == 2)
    }
  }
}

@SpringBootApplication(exclude = Array(classOf[ElasticsearchAutoConfiguration], classOf[ElasticsearchDataAutoConfiguration]))
@Import(Array(classOf[MongoConfig]))
class SecondConfig
