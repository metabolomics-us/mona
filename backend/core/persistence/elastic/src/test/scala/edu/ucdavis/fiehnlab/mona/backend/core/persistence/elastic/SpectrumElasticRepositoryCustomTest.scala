package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper.config.EmbeddedElasticSearchConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.repository.ISpectrumElasticRepositoryCustom
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.{RSQLRepositoryCustom, RSQLRepositoryCustomTest}
import org.elasticsearch.index.query.QueryBuilder
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterEach
import org.springframework.beans.factory.annotation.{Autowired, Qualifier}
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.repository.CrudRepository
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.{ContextConfiguration, TestContextManager}

/**
  * Created by wohlg_000 on 3/9/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[EmbeddedElasticSearchConfiguration]))
class SpectrumElasticRepositoryCustomTest extends RSQLRepositoryCustomTest[Spectrum,QueryBuilder] with BeforeAndAfterEach  {
  @Autowired
  val elasticsearchTemplate: ElasticsearchTemplate = null

  @Autowired
  @Qualifier("spectrumElasticRepository")
  val spectrumElasticRepository: ISpectrumElasticRepositoryCustom = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  override def getRepository: RSQLRepositoryCustom[Spectrum,QueryBuilder] with CrudRepository[Spectrum, String] = spectrumElasticRepository

}
