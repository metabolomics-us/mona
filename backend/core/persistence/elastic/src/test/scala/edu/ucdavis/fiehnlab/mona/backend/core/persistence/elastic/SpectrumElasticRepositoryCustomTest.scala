package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Spectrum, Tags}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.TestConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.{RSQLRepositoryCustom, RSQLRepositoryCustomTest}
import org.elasticsearch.index.query.QueryBuilder
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterEach
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.repository.CrudRepository
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager, TestPropertySource}

/**
  * Created by wohlg_000 on 3/9/2016.
  */
@RunWith(classOf[SpringRunner])
@ContextConfiguration(classes = Array(classOf[TestConfig]))
@TestPropertySource(locations = Array("classpath:application.properties"))
class SpectrumElasticRepositoryCustomTest extends RSQLRepositoryCustomTest[Spectrum, QueryBuilder] with BeforeAndAfterEach {

  @Autowired
  val elasticsearchTemplate: ElasticsearchTemplate = null

  @Autowired
  @Qualifier("spectrumElasticRepository")
  val spectrumElasticRepository: ISpectrumElasticRepositoryCustom = null

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override def getRepository: RSQLRepositoryCustom[Spectrum, QueryBuilder] with CrudRepository[Spectrum, String] = spectrumElasticRepository

  "elastic specific queries " should {

    s"we should be able to reload our data" in {
      exampleRecords.foreach { spectrum =>
        val size = getRepository.count()

        getRepository.save(spectrum)
        val newSize = getRepository.count()

        assert(newSize == size + 1)
      }
    }

    "we should be able to query for complex tag names" in {
      val spectrum: Spectrum = exampleRecords.head
      val query: String = """tags.text=="test - unknown - unknown - positive""""

      getRepository.save(spectrum.copy(id = "test", tags = spectrum.tags ++ Array(Tags(ruleBased = false, "test - unknown - unknown - positive"))))
      assert(getRepository.exists("test"))

      val result = getRepository.query(query, "")
      assert(result.size() == 1)
      assert(getRepository.queryCount(query, "") == 1)
    }
  }
}
