package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.config.ElasticsearchConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.{RSQLRepositoryCustomTest, RSQLRepositoryCustom}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.springframework.beans.factory.annotation.{Qualifier, Autowired}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.repository.CrudRepository
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * Created by wohlg_000 on 3/9/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(Array(classOf[ElasticsearchConfig]))
@ComponentScan
@EnableAutoConfiguration
class SpectrumElasticRepositoryCustomTest  extends RSQLRepositoryCustomTest[Spectrum] {

  @Autowired
  @Qualifier("spectrumElasticRepository")
  val spectrumElasticRepository: ISpectrumElasticRepositoryCustom = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  override def getRepository: RSQLRepositoryCustom[Spectrum] with CrudRepository[Spectrum, String] = spectrumElasticRepository

}
