package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views

import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import cz.jirutka.rsql.parser.RSQLParser
import cz.jirutka.rsql.parser.ast.{ComparisonOperator, Node}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.SpectrumResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.domain.Specification
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.domain.views.SearchTable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumResultRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views.SearchTableRepository.SparseSearchTable
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.rsql.{CustomRsqlVisitor, RSQLOperatorsCustom}
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import java.io.InputStreamReader

@SpringBootTest
@ActiveProfiles(Array("test"))
class SearchTableRepositoryTest extends AnyWordSpec with Matchers with LazyLogging {
  @Autowired
  val searchTableRepository: SearchTableRepository = null

  @Autowired
  val spectrumResultsRepository: SpectrumResultRepository = null

  @Autowired
  val mapper: ObjectMapper = null

  val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))
  val curatedRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")))
  val operators: java.util.Set[ComparisonOperator] = RSQLOperatorsCustom.newDefaultOperators()

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Search Table Repository is working" when {
    "issues standard curd commands" should {
      "we should be able to empty database" in {
        spectrumResultsRepository.deleteAll()
        assert(spectrumResultsRepository.count() == 0)
      }

      s"we should be able to load data" in {
        assert(spectrumResultsRepository.count() == 0)
        exampleRecords.foreach { spectrum =>
          val serialized = mapper.writeValueAsString(spectrum)
          spectrumResultsRepository.save(new SpectrumResult(spectrum.id, serialized))
        }
        assert(spectrumResultsRepository.count() == 59)
      }

      "we should be able to execute RSQL queries like 'inchikey==GHSJKUNUIHUPDF-BYPYZUCNSA-N'" in {
        val rootNode: Node = new RSQLParser(operators).parse("inchikey==GHSJKUNUIHUPDF-BYPYZUCNSA-N and compoundKind==biological")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,50))
        assert(results.getTotalElements == 1)
      }

      "we should be able to execute RSQL queries like metadataName==\'compound_name\' and metadataValue==\'META-HYDROXYBENZOIC ACID\'" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName==\'compound_name\' and metadataValue==\'META-HYDROXYBENZOIC ACID\'")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,50))
        assert(results.getTotalElements == 1)
      }

      "we should be able to execute RSQL queries like block1=='splash10'" in {
        val rootNode: Node = new RSQLParser(operators).parse("block1=='splash10'")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 59)
      }

      "we should be able to execute RSQL queries like splash.block1=='splash10' with pagination" in {
        val rootNode: Node = new RSQLParser(operators).parse("block1=='splash10'")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,10))
        assert(results.getContent.size() == 10)
        assert(results.getTotalPages == 6)
      }

      "we should be able query by monaId==\"3488925\"" in {
        val rootNode: Node = new RSQLParser(operators).parse("monaId==\"3488925\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,10))
        assert(results.getTotalElements == 1)
      }

      "we should be able to execute RSQL queries like metadataName==\"license\" and metadataValue==\"CC BY-SA\"" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName==\"license\" and metadataValue==\"CC BY-SA\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 58)
      }

      "we should be able to execute RSQL queries like metadataName==\"total exact mass\" and metadataValue=gt=306 and metadataValue=lt=307" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName==\"total exact mass\" and metadataValue=gt=306 and metadataValue=lt=307")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 2)
      }


      "we should be able to execute RSQL queries like metadataName==\"total exact mass\" and metadataValue=gt=306.07 and metadataValue=lt=306.08" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName==\"total exact mass\" and metadataValue=gt=306.07 and metadataValue=lt=306.08")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 2)
      }

      "we should be able to execute RSQL queries like metadataName==\"ion mode\" and metadataValue==negative" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName==\"ion mode\" and metadataValue==negative")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 25)
      }

      "we should be able to execute RSQL queries like text==LCMS" in {
        val rootNode: Node = new RSQLParser(operators).parse("text==LCMS")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 58)
      }

      "we should be able to execute RSQL queries like metadataName==\"collision energy\" and metadataValue==\"35%\"" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName==\"collision energy\" and metadataValue==\"35%\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 0)
      }

      "possible to execute the same query several times and receive always the same result" must {

        "support pageable sizes of 1" in {
          var last: SparseSearchTable = null

          for (_ <- 1 to 3) {
            val rootNode: Node = new RSQLParser(operators).parse("text=like=LCMS")
            val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
            val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,1))
            val current: SparseSearchTable = results.iterator().next()

            if (last == null) {
              last = current
            }

            logger.info(s"received spectrum is ${current.getMonaId}")
            assert(last.getMonaId == current.getMonaId)
          }
        }
      }

      "we should be able to store additional, curated records" in {
        curatedRecords.foreach { spectrum =>
          val size = spectrumResultsRepository.count()
          val serialized = mapper.writeValueAsString(spectrum)
          spectrumResultsRepository.save(new SpectrumResult(spectrum.id, serialized))
          val newSize = spectrumResultsRepository.count()
          assert(newSize == size + 1)
        }
      }


      s"we should have ${curatedRecords.length + exampleRecords.length} records in the repository now" in {
        assert(spectrumResultsRepository.count() == curatedRecords.length + exampleRecords.length)
      }

      "we should be able to execute RSQL queries like metadataName==\"class\" and metadataValue==\"Benzenoids\" in" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName==\"class\" and metadataValue==\"Benzenoids\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 41)
      }


      "we should be able to execute RSQL queries like metadataValue==\"Benzenoids\" in" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataValue==\"Benzenoids\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 45)
      }

      "we should be able to execute RSQL queries like metadataName==\"C4H5+\" in" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName==\"C4H5+\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 13)
      }

      "we should be able to execute RSQL queries like metadataName==\"C4H5+\" and metadataValue==53.0386 in" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName==\"C4H5+\" and metadataValue==53.0386")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 3)
      }

      "we should be able to execute RSQL queries like emailAddress==\"ML@MassBank.jp\" in" in {
        exampleRecords.map(_.submitter.emailAddress).toSet.foreach { emailAddress: String =>
          val rootNode: Node = new RSQLParser(operators).parse(s"emailAddress==$emailAddress")
          val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
          val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,1))
          assert(!results.isEmpty)
        }
      }


      // Ensure that =like= queries work
      "we should be able to execute RSQL queries like metadataName=like=\"%mode%\" and metadataValue==negative in " in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName=like=\"%mode%\" and metadataValue==negative")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 29)
      }

      "we should be able to execute RSQL queries like metadataName==\"ion mode\" and metadataValue=like=negative in " in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName==\"ion mode\" and metadataValue=like=\"%negative%\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 25)
      }

      "we should be able to execute RSQL queries like metadataValue=like=\"%hydroxybenzoic%\" in " in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataValue=like=\"%hydroxybenzoic%\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 4)
      }

      "we should be able to execute RSQL queries like metadataValue=like=\"%HYDROXYBENZOIC%\" in " in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataValue=like=\"%HYDROXYBENZOIC%\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 4)
      }

      "we should be able to execute RSQL queries like metadataValue=like=\"%HYDROXYBE%\" in " in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataValue=like=\"%HYDROXYBE%\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 5)
      }

      "we should be able to execute RSQL queries like metadataName==class and metadataValue=like=\"%Benzenoids%\" in" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataName==class and metadataValue=like=\"%Benzenoids%\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,50))
        assert(results.getTotalElements == 41)
      }

      "we should be able to execute RSQL queries like metadataValue=like=\"%Benzenoids%\" in" in {
        val rootNode: Node = new RSQLParser(operators).parse("metadataValue=like=\"%Benzenoids%\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 45)
      }

      "we should be able to execute RSQL queries like text=like=\"%lcms\"% in" in {
        val rootNode: Node = new RSQLParser(operators).parse("text=like=\"%lcms%\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 58)
      }

      "we should be able to execute RSQL queries like text=like=\"%lc-ms%\" in" in {
        val rootNode: Node = new RSQLParser(operators).parse("text=like=\"%lc-ms%\"")
        val spec: Specification[SearchTable] = rootNode.accept(new CustomRsqlVisitor[SearchTable]())
        val results: Page[SparseSearchTable] = searchTableRepository.findAll(spec, classOf[SparseSearchTable], PageRequest.of(0,60))
        assert(results.getTotalElements == 51)
      }


      "possible to delete one object" in {
        assert(spectrumResultsRepository.count() == curatedRecords.length + exampleRecords.length)
        val one = spectrumResultsRepository.findAll().iterator().next()
        spectrumResultsRepository.delete(one)
        assert(curatedRecords.length + exampleRecords.length - 1 == spectrumResultsRepository.count())
      }

      "possible to delete all data" in {
        spectrumResultsRepository.deleteAll()
        assert(spectrumResultsRepository.count() == 0)
      }
    }
  }
}
