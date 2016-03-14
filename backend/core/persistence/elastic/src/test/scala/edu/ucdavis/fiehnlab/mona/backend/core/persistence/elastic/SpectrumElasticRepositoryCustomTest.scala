package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic

import java.lang.Iterable

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.EmbeddedElasticSearchConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.{RSQLRepositoryCustomTest, RSQLRepositoryCustom}
import org.elasticsearch.index.query.{FilterBuilder, QueryBuilder}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterEach, Ignore, FunSuite}
import org.springframework.beans.factory.annotation.{Qualifier, Autowired}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.repository.CrudRepository
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import scala.collection.JavaConverters

/**
  * Created by wohlg_000 on 3/9/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(Array(classOf[EmbeddedElasticSearchConfiguration]))
@ComponentScan
@EnableAutoConfiguration
//@Ignore
class SpectrumElasticRepositoryCustomTest extends RSQLRepositoryCustomTest[Spectrum,FilterBuilder] with BeforeAndAfterEach  {

  @Autowired
  @Qualifier("spectrumElasticRepository")
  val spectrumElasticRepository: ISpectrumElasticRepositoryCustom = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  override def getRepository: RSQLRepositoryCustom[Spectrum,FilterBuilder] with CrudRepository[Spectrum, String] = spectrumElasticRepository

  "we should be able to call the custom methods here" should {

    "load all data" in {
      val result: Iterable[Spectrum] = spectrumElasticRepository.findAll()

      assert(spectrumElasticRepository.count() == 58)

      val it = result.iterator()
      assert(it.hasNext)
    }

    "for example the find by inchi key" in {
      val result: Iterable[Spectrum] = spectrumElasticRepository.findByBiologicalCompoundInchiKey("UYTPUPDQBNUYGX-UHFFFAOYSA-N")

      val it = result.iterator()

      assert(it.hasNext)

    }
  }
}
