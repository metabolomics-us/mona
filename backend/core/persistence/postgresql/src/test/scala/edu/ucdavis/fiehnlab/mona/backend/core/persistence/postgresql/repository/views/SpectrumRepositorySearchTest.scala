package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.turkraft.springfilter.FilterParameters
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.domain.Specification
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.filter.FilterSpecificationDistinct
import org.hibernate.Hibernate
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.Eventually
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.test.context.{ActiveProfiles, TestContextManager}
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

import java.io.InputStreamReader
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.jdk.CollectionConverters._

@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SpectrumRepositorySearchTest extends AnyWordSpec with Matchers with LazyLogging with Eventually with BeforeAndAfterEach{
  @Autowired
  val spectrumRepository: SpectrumRepository = null

  @Autowired
  val monaMapper: ObjectMapper = {
    MonaMapper.create
  }

    @Autowired private val transactionManager: PlatformTransactionManager = null

    private var transactionTemplate: TransactionTemplate = null

  val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")), new TypeReference[Array[Spectrum]] {})
  val curatedRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/curatedRecords.json")), new TypeReference[Array[Spectrum]] {})

  new TestContextManager(this.getClass).prepareTestInstance(this)

  protected override def beforeEach() = (
        transactionTemplate = new TransactionTemplate(transactionManager)
        )

  protected override def afterEach() = (
    transactionTemplate = null
    )

  "Search Table Repository is working" when {
    "issues standard curd commands" should {
      "we should be able to empty database" in {
        spectrumRepository.deleteAll()
        assert(spectrumRepository.count() == 0)
      }

      s"we should be able to load data" in {
        assert(spectrumRepository.count() == 0)
        exampleRecords.foreach { spectrum =>
          spectrumRepository.save(spectrum)
        }
        assert(spectrumRepository.count() == 59)
      }

      "we should be able to execute RSQL queries like compound.inchiKey~'GHSJKUNUIHUPDF-BYPYZUCNSA-N' and compound.kind~'biological'" in {

        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("compound.inchiKey~'GHSJKUNUIHUPDF-BYPYZUCNSA-N' and compound.kind~'biological'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,50))
        assert(results.getTotalElements == 1)
      }

      "we should be able to execute RSQL queries like compound.names.name~'META-HYDROXYBENZOIC ACID'" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("compound.names.name~'META-HYDROXYBENZOIC ACID'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,50))
        assert(results.getTotalElements == 1)
      }

      "we should be able to execute RSQL queries like splash.block1~'splash10'" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("splash.block1~'splash10'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 59)
      }

      "we should be able to execute RSQL queries like splash.block1~'splash10' with pagination" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("splash.block1~'splash10'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,10))
        assert(results.getContent.size() == 10)
        assert(results.getTotalPages == 6)
      }

      "we should be able query by id~'3488925'" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("id~'3488925'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,10))
        assert(results.getTotalElements == 1)
      }

      "we should be able to execute RSQL queries like metaData.name~'license' and metaData.value~'CC BY-SA'" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("metaData.name~'license' and metaData.value~'CC BY-SA'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 58)
      }

      "we should be able to execute RSQL queries like compound.metaData.name~'total exact mass' and compound.metaData.value>'306' and compound.metaData.value<'307'" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("compound.metaData.name~'total exact mass' and compound.metaData.value>'306' and compound.metaData.value<'307'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 2)
      }


      "we should be able to execute RSQL queries like compound.metaData.name~'total exact mass' and compound.metaData.value>'306.07' and compound.metaData.value<'306.08'" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("compound.metaData.name~'total exact mass' and compound.metaData.value>'306.07' and compound.metaData.value<'306.08'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 2)
      }

      "we should be able to execute RSQL queries like \"metaData.name~'ion mode' and metaData.value~'negative'\"" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("metaData.name~'ion mode' and metaData.value~'negative'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 25)
      }
//
      "we should be able to execute RSQL queries like \"tags.text~'LCMS'\"" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("tags.text~'LCMS'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 58)
      }

      "we should be able to execute RSQL queries like metaData.name:'collision energy' and metaData.value:'35%'" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("metaData.name:'collision energy' and metaData.value:'35%'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 0)
      }

      "possible to execute the same query several times and receive always the same result" must {

        "support pageable sizes of 1" in {
          var last: Spectrum = null

          for (_ <- 1 to 3) {
            val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("tags.text~'*LCMS*'")
            val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,1))
            val current: Spectrum = results.iterator().next()

            if (last == null) {
              last = current
            }

            logger.info(s"received spectrum is ${current.getId}")
            assert(last.getId == current.getId)
          }
        }
      }

      "we should be able to store additional, curated records" in {
        spectrumRepository.saveAll(curatedRecords.toIterable.asJava)
        val newSize: Long = spectrumRepository.count()
         assert(newSize == 109)
      }


      s"we should have ${curatedRecords.length + exampleRecords.length} records in the repository now" in {
        assert(spectrumRepository.count() == curatedRecords.length + exampleRecords.length)
      }

      "we should be able to execute RSQL queries like \"compound.classification.name:'class' and compound.classification.value:'Benzenoids'\" in" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("compound.classification.name~'class' and compound.classification.value~'Benzenoids'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 41)
      }


      "we should be able to execute RSQL queries like compound.classification.value~'Benzenoids' in" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("compound.classification.value~'Benzenoids'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 45)
      }

      "we should be able to execute RSQL queries like annotations.name~'C4H5+' in" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("annotations.name~'C4H5+'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 13)
      }

      "we should be able to execute RSQL queries like annotations.name~'C4H5+' and annotations.value~'53.0386'in" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("annotations.name~'C4H5+' and annotations.value~'53.0386'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 3)
      }

      "we should be able to execute RSQL queries like submitter.emailAddress~'ML@MassBank.jp' in" in {
        exampleRecords.map(_.getSubmitter.getEmailAddress).toSet.foreach { emailAddress: String =>
          val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum](s"submitter.emailAddress~'$emailAddress'")
          val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,1))
          assert(!results.isEmpty)
        }
      }


      // Ensure that =like= queries work
      "we should be able to execute RSQL queries like metaData.name~'*mode*' and metaData.value~'negative' in " in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("metaData.name~'*mode*' and metaData.value~'negative'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 29)
      }

      "we should be able to execute RSQL queries like metaData.name~'ion mode' and metaData.value~'*negative*' in " in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("metaData.name~'ion mode' and metaData.value~'*negative*'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 25)
      }

      "we should be able to execute RSQL queries like compound.names.name~'*hydroxybenzoic*' in " in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("compound.names.name~'*hydroxybenzoic*'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 4)
      }

      "we should be able to execute RSQL queries like compound.names.name~'*HYDROXYBENZOIC*' in " in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("compound.names.name~'*HYDROXYBENZOIC*'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 4)
      }

      "we should be able to execute RSQL queries like compound.names.name~'*HYDROXYBE*' in " in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("compound.names.name~'*HYDROXYBE*'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 5)
      }

      "we should be able to execute RSQL queries like compound.classification.name~'class' and compound.classification.value~'*Benzenoids*' in" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("compound.classification.name~'class' and compound.classification.value~'*Benzenoids*'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,50))
        assert(results.getTotalElements == 41)
      }

      "we should be able to execute RSQL queries like compound.classification.value~'*Benzenoids*' in" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("compound.classification.value~'*Benzenoids*'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 45)
      }

      "we should be able to execute RSQL queries like tags.text~'*lcms*' in" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("tags.text~'*lcms*'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 58)
      }

      "we should be able to execute RSQL queries like tags.text~'*lc-ms*' in" in {
        val spec: Specification[Spectrum] = new FilterSpecificationDistinct[Spectrum]("tags.text~'*lc-ms*'")
        val results: Page[Spectrum] = spectrumRepository.findAll(spec, PageRequest.of(0,60))
        assert(results.getTotalElements == 51)
      }


      "possible to delete one object" in {
        assert(spectrumRepository.count() == curatedRecords.length + exampleRecords.length)
        val one = spectrumRepository.findAll().iterator().next()
        spectrumRepository.delete(one)
        assert(curatedRecords.length + exampleRecords.length - 1 == spectrumRepository.count())
      }

      "possible to delete all data" in {
        spectrumRepository.deleteAll()
        assert(spectrumRepository.count() == 0)
      }
    }
  }
}
