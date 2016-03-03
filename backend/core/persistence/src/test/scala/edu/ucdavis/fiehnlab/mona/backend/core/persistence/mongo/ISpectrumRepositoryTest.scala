package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import java.io.{File, FileReader}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.{Splash, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config.RepositoryConfiguration
import org.junit.runner.RunWith
import org.scalatest.{WordSpec, FunSuite}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 2/26/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(classes = Array(classOf[RepositoryConfiguration]))
class ISpectrumRepositoryTest extends WordSpec {

  @Autowired
  val spectrumRepository: ISpectrumRepositoryCustom = null

  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)


  "a repository is loaded with 58 compounds" when {


    //58 spectra for us to work with
    val exampleRecords: Array[Spectrum] = JSONDomainReader.create[Array[Spectrum]].read(new FileReader(new File("src/test/resources/monaRecords.json")))

    "reading our records" should {
      s"results in ${exampleRecords.length} records" in {
        assert(exampleRecords.length == 58)
      }
    }
    spectrumRepository.deleteAll()

    assert(spectrumRepository.count() == 0)

    "issues standard crud commands " should {

      for (spectrum <- exampleRecords) {
        val size = spectrumRepository.count()

        spectrumRepository.save(spectrum)
        val newSize = spectrumRepository.count()

        assert(newSize == size + 1)
      }

      s" increase the amount of data in the system by ${exampleRecords.length}" in {
        assert(spectrumRepository.count() == exampleRecords.length)
      }

      "provide us with a custom way to query data" in {
        val result = spectrumRepository.executeQuery(new BasicQuery(s"""{"biologicalCompound.inchiKey" : "KKSDGJDHHZEWEP-UHFFFAOYSA-N"}"""))

        assert(result.size == 1)

        assert(result.asScala.head.biologicalCompound.inchiKey == "KKSDGJDHHZEWEP-UHFFFAOYSA-N")
      }

      "provide us with the possibility to query data, by providing a string and query in a range of double values" in {

        val result:java.util.List[Spectrum] = spectrumRepository.executeQuery("""{"biologicalCompound.metaData" : {$elemMatch : { name : "total exact mass", value : { $gt:164.047, $lt:164.048} } } }""")

        assert(result.size == 1)
      }

      "provide us with the possibility to query data, for a specific metadata filed" in {

        val result:java.util.List[Spectrum] = spectrumRepository.executeQuery("""{"biologicalCompound.metaData" : {$elemMatch : { name : "BioCyc", value : "CYTIDINE" } } }""")

        assert(result.size == 2)
      }

      "provide us with the possibility to query data, by a tag query" in {

        val result:java.util.List[Spectrum] = spectrumRepository.executeQuery("""{"tags" : {$elemMatch : { text : "LCMS" } } }""")

        assert(result.size == 58)

      }

      "provide us with the possibility to query all data and paginate it" in {
        val page:Page[Spectrum] = spectrumRepository.findAll(new PageRequest(0,30))

        assert(page.isFirst)
        assert(page.getTotalElements == 58)
        assert(page.getTotalPages == 2)

        val page2:Page[Spectrum] = spectrumRepository.findAll(new PageRequest(30,60))

        assert(page2.isLast)

      }

      "provide us with the possibility to query custom queries all data and paginate it" in {

        val page:Page[Spectrum] = spectrumRepository.executeQuery("""{"tags" : {$elemMatch : { text : "LCMS" } } }""",new PageRequest(0,30))

        assert(page.isFirst)
        assert(page.getTotalElements == 58)
        assert(page.getTotalPages == 2)

        val page2:Page[Spectrum] = spectrumRepository.executeQuery("""{"tags" : {$elemMatch : { text : "LCMS" } } }""",new PageRequest(30,60))

        assert(page2.isLast)

      }

      "we should be able to update a spectra with new properties" in {
        val spectrum = spectrumRepository.findAll().asScala.head

        val splash:Splash = spectrum.splash.copy(splash = "tada")
        val spectrum2:Spectrum = spectrum.copy(splash = splash)

        val countBefore = spectrumRepository.count()

        spectrumRepository.save(spectrum2)

        val countAfter = spectrumRepository.count()

        val spectrum3 = spectrumRepository.findOne(spectrum2.id)

        assert(spectrum3.splash.splash == spectrum2.splash.splash)
        assert(countBefore == countAfter)
      }
    }
  }

}