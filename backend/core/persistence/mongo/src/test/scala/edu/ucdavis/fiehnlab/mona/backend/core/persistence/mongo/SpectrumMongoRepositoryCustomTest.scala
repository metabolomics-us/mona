package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.{Splash, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.EmbeddedMongoDBConfiguration
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.{RSQLRepositoryCustom, RSQLRepositoryCustomTest}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.{Qualifier, Autowired}
import org.springframework.data.domain.{PageRequest, Page}
import org.springframework.data.mongodb.core.query.{BasicQuery, Query}
import org.springframework.data.repository.CrudRepository
import org.springframework.test.context.{ContextConfiguration, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import scala.collection.JavaConverters._

/**
  * Created by wohlg_000 on 3/9/2016.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(classes = Array(classOf[EmbeddedMongoDBConfiguration]))
class SpectrumMongoRepositoryCustomTest extends RSQLRepositoryCustomTest[Spectrum,Query] {

  @Autowired
  @Qualifier("spectrumMongoRepository")
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  override def getRepository: RSQLRepositoryCustom[Spectrum,Query] with CrudRepository[Spectrum, String] = spectrumMongoRepository

  "mongo specific queries " should {

    "provide us with the possibility to query data, by providing a string and query in a range of double values" in {

      val result: java.util.List[Spectrum] = spectrumMongoRepository.nativeQuery(new BasicQuery("""{"biologicalCompound.metaData" : {$elemMatch : { name : "total exact mass", value : { $gt:164.047, $lt:164.048} } } }"""))
      assert(result.size == 1)
    }

    "provide us with the possibility to query data, for a specific metadata filed" in {

      val result: java.util.List[Spectrum] = spectrumMongoRepository.nativeQuery(new BasicQuery("""{"biologicalCompound.metaData" : {$elemMatch : { name : "BioCyc", value : "CYTIDINE" } } }"""))

      assert(result.size == 2)
    }

    "provide us with the possibility to query data, by a tag query" in {

      val result: java.util.List[Spectrum] = spectrumMongoRepository.nativeQuery(new BasicQuery("""{"tags" : {$elemMatch : { text : "LCMS" } } }"""))

      assert(result.size == 58)

    }

    "provide us with the possibility to query all data and paginate it" in {
      val page: Page[Spectrum] = spectrumMongoRepository.findAll(new PageRequest(0, 30))

      assert(page.isFirst)
      assert(page.getTotalElements == 58)
      assert(page.getTotalPages == 2)

      val page2: Page[Spectrum] = spectrumMongoRepository.findAll(new PageRequest(30, 60))

      assert(page2.isLast)

    }

    "provide us with the possibility to query custom queries all data and paginate it" in {

      val page: Page[Spectrum] = spectrumMongoRepository.nativeQuery(new BasicQuery("""{"tags" : {$elemMatch : { text : "LCMS" } } }"""), new PageRequest(0, 30))

      assert(page.isFirst)
      assert(page.getTotalElements == 58)
      assert(page.getTotalPages == 2)

      val page2: Page[Spectrum] = spectrumMongoRepository.nativeQuery(new BasicQuery("""{"tags" : {$elemMatch : { text : "LCMS" } } }"""), new PageRequest(30, 60))

      assert(page2.isLast)

    }

    "we should be able to update a spectra with new properties" in {
      val spectrum = spectrumMongoRepository.findAll().asScala.head

      val splash: Splash = spectrum.splash.copy(splash = "tada")
      val spectrum2: Spectrum = spectrum.copy(splash = splash)

      val countBefore = spectrumMongoRepository.count()

      spectrumMongoRepository.save(spectrum2)

      val countAfter = spectrumMongoRepository.count()

      val spectrum3 = spectrumMongoRepository.findOne(spectrum2.id)

      assert(spectrum3.splash.splash == spectrum2.splash.splash)
      assert(countBefore == countAfter)
    }

  }
}
