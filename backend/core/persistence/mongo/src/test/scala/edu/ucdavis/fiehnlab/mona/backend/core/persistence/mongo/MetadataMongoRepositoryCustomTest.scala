package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.{Splash, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.{RSQLRepositoryCustom, RSQLRepositoryCustomTest}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.{Qualifier, Autowired}
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.data.domain.{PageRequest, Page}
import org.springframework.data.mongodb.core.query.{BasicQuery, Query}
import org.springframework.data.repository.CrudRepository
import org.springframework.test.context.{ContextConfiguration, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import scala.collection.JavaConverters._

@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[EmbeddedMongoDBConfiguration]))
class MetadataMongoRepositoryCustomTest extends RSQLRepositoryCustomTest[Spectrum,Query] {

  @Autowired
  @Qualifier("spectrumMongoRepository")
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  override def getRepository: RSQLRepositoryCustom[Spectrum,Query] with CrudRepository[Spectrum, String] = spectrumMongoRepository

  "Metadata aggregation queries" when {
    "given a specific metadata field" must {
      "return the top records in the base metadata group" in {
        val msLevel = spectrumMongoRepository.listTopAggregates("ms level")
        assert(msLevel.nonEmpty)
        assert(msLevel.mkString == "(MS2,58)")

        val ionMode = spectrumMongoRepository.listTopAggregates("ion mode")
        assert(ionMode.nonEmpty)
        assert(ionMode.mkString == "(positive,33)(negative,25)")
      }

      "throw an exception when given a null metadata field name" in {
        intercept[IllegalArgumentException] { 
          spectrumMongoRepository.listTopAggregates(null) 
        }
      }
    }

    "given a specific metadata field and an offset and limit" should {
      "limit the maximum number of results" in {
        val total = 10
        val pageSize = total / 2

        val r0 = spectrumMongoRepository.listTopAggregates("BioCyc", skip = 0, limit = total, metaDataGroup = Some("biologicalCompound"))
        val r1 = spectrumMongoRepository.listTopAggregates("BioCyc", limit = pageSize, metaDataGroup = Some("biologicalCompound"))
        val r2 = spectrumMongoRepository.listTopAggregates("BioCyc", pageSize, pageSize, Some("biologicalCompound"))
        // TODO This should work if the pagination/sorting problem is addressed
        assert(r0.length == total)
        assert(r1.length == pageSize)
        assert(r2.length == pageSize)
        assert(r0.take(pageSize) == r1)
        assert(r0.drop(pageSize) == r2)
      }
      
      "address the pagination/sorting problem" in {
        val field = "BioCyc"
        val group = Some("biologicalCompound")
        val total = 10

        println("skip 0:\n\t" + spectrumMongoRepository.listTopAggregates(field, 0, total, group).mkString("\n\t"))
        println("skip 1:\n\t" + spectrumMongoRepository.listTopAggregates(field, 1, total, group).mkString("\n\t"))
        println("skip 2:\n\t" + spectrumMongoRepository.listTopAggregates(field, 2, total, group).mkString("\n\t"))

        assert(false)
      }

      "not return more then the actual number of results" in {
        val result = spectrumMongoRepository.listTopAggregates("ion mode", 0, 5)
        assert(result.length == 2)
      }

      "throw an exception when given invalid lower bounds" in {
        intercept[IllegalArgumentException] { 
          spectrumMongoRepository.listTopAggregates("ion mode", -1, 10) 
        }
        intercept[InvalidDataAccessApiUsageException] { 
          spectrumMongoRepository.listTopAggregates("ion mode", 0, 0)
        }
      }
    }

    "given a specific metadata field and metadata group" must {
      "return the top records for the metadata field/group" in {
        val result = spectrumMongoRepository.listTopAggregates("total exact mass", metaDataGroup = Some("biologicalCompound"))
        assert(result.nonEmpty)
      }
    }
  }
}
