package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import cz.jirutka.rsql.parser.RSQLParser
import cz.jirutka.rsql.parser.ast.Node
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.domain.Specification
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.Compound
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.rsql.CustomRsqlVisitor
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import java.io.InputStreamReader

@SpringBootTest
@ActiveProfiles(Array("test"))
class CompoundRepositoryTest extends AnyWordSpec with Matchers with LazyLogging{
  @Autowired
  val compoundRepository: CompoundRepository = null

  @Autowired
  val spectrumResultsRepository: SpectrumResultRepository = null

  @Autowired
  val mapper: ObjectMapper = null

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  new TestContextManager(this.getClass).prepareTestInstance(this)

  /*
  "Compound Repository" should {
    s"empty database" must {
      "with deleteAll" in {
        spectrumResultsRepository.deleteAll()
        assert(spectrumResultsRepository.count() == 0)
      }
    }

    s"load data" in {
      assert(spectrumResultsRepository.count() == 0)
      exampleRecords.foreach { spectrum =>
        val serialized = mapper.writeValueAsString(spectrum)
        spectrumResultsRepository.save(new SpectrumResult(spectrum.id, serialized))
      }
      assert(spectrumResultsRepository.count() == 59)
    }
    s"be able to get" must {
      "kind" in {
        val rootNode: Node = new RSQLParser().parse("kind==observed")
        val spec: Specification[Compound] = rootNode.accept(new CustomRsqlVisitor[Compound]())
        val results: java.util.List[Compound] = compoundRepository.findAll(spec)
        results.forEach { result =>
          logger.info(s"${result.getMonaId}")
          logger.info(s"${result.getNames}")
        }
        assert(results.size() == 58)
      }
    }
  }
  */

}
