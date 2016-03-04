package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import java.io.{File, FileReader}

import com.mongodb.{MongoClient, Mongo}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.{Splash, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, WordSpec, FunSuite}
import org.springframework.beans.factory.annotation.{Qualifier, Value, Autowired}
import org.springframework.boot.autoconfigure.{SpringBootApplication, EnableAutoConfiguration}
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.annotation._
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.data.mongodb.config.AbstractMongoConfiguration
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.test.context.{ContextConfiguration, TestContextManager}
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration
import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 2/26/16.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringApplicationConfiguration(Array(classOf[MyTestConfig]))
@ComponentScan
@EnableAutoConfiguration
class ISpectrumMongoRepositoryTest extends WordSpec{

  @Autowired
  @Qualifier("spectrumMongoRepository")
  val spectrumMongoRepository: ISpectrumMongoRepositoryCustom = null

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
    spectrumMongoRepository.deleteAll()

    assert(spectrumMongoRepository.count() == 0)

    "issues standard crud commands " should {

      for (spectrum <- exampleRecords) {
        val size = spectrumMongoRepository.count()

        spectrumMongoRepository.save(spectrum)
        val newSize = spectrumMongoRepository.count()

        assert(newSize == size + 1)
      }

      s" increase the amount of data in the system by ${exampleRecords.length}" in {
        assert(spectrumMongoRepository.count() == exampleRecords.length)
      }

      "provide us with the possibility to query data, by providing a string and query in a range of double values" in {

        val result:java.util.List[Spectrum] = spectrumMongoRepository.nativeQuery("""{"biologicalCompound.metaData" : {$elemMatch : { name : "total exact mass", value : { $gt:164.047, $lt:164.048} } } }""")
        assert(result.size == 1)
      }

      "provide us with the possibility to query data, for a specific metadata filed" in {

        val result:java.util.List[Spectrum] = spectrumMongoRepository.nativeQuery("""{"biologicalCompound.metaData" : {$elemMatch : { name : "BioCyc", value : "CYTIDINE" } } }""")

        assert(result.size == 2)
      }

      "provide us with the possibility to query data, by a tag query" in {

        val result:java.util.List[Spectrum] = spectrumMongoRepository.nativeQuery("""{"tags" : {$elemMatch : { text : "LCMS" } } }""")

        assert(result.size == 58)

      }

      "provide us with the possibility to query all data and paginate it" in {
        val page:Page[Spectrum] = spectrumMongoRepository.findAll(new PageRequest(0,30))

        assert(page.isFirst)
        assert(page.getTotalElements == 58)
        assert(page.getTotalPages == 2)

        val page2:Page[Spectrum] = spectrumMongoRepository.findAll(new PageRequest(30,60))

        assert(page2.isLast)

      }

      "provide us with the possibility to query custom queries all data and paginate it" in {

        val page:Page[Spectrum] = spectrumMongoRepository.nativeQuery("""{"tags" : {$elemMatch : { text : "LCMS" } } }""",new PageRequest(0,30))

        assert(page.isFirst)
        assert(page.getTotalElements == 58)
        assert(page.getTotalPages == 2)

        val page2:Page[Spectrum] = spectrumMongoRepository.nativeQuery("""{"tags" : {$elemMatch : { text : "LCMS" } } }""",new PageRequest(30,60))

        assert(page2.isLast)

      }

      "we should be able to update a spectra with new properties" in {
        val spectrum = spectrumMongoRepository.findAll().asScala.head

        val splash:Splash = spectrum.splash.copy(splash = "tada")
        val spectrum2:Spectrum = spectrum.copy(splash = splash)

        val countBefore = spectrumMongoRepository.count()

        spectrumMongoRepository.save(spectrum2)

        val countAfter = spectrumMongoRepository.count()

        val spectrum3 = spectrumMongoRepository.findOne(spectrum2.id)

        assert(spectrum3.splash.splash == spectrum2.splash.splash)
        assert(countBefore == countAfter)
      }

      "we should be able to execute RSQL queries like biologicalCompound.inchiKey==?" in {
        val spectrum = spectrumMongoRepository.findAll().asScala.head

        val result = spectrumMongoRepository.rsqlQuery(s"biologicalCompound.inchiKey==${spectrum.biologicalCompound.inchiKey}")

        assert(result.size() == 1)
      }

      "we should be able to execute RSQL queries like biologicalCompound.names.name=='META-HYDROXYBENZOIC ACID'" in {
        val result = spectrumMongoRepository.rsqlQuery(s"biologicalCompound.names.name=='META-HYDROXYBENZOIC ACID'")
        assert(result.size() == 1)
      }

      "we should be able to execute RSQL queries like biologicalCompound.names.name=='*ACID'" ignore {
        val result = spectrumMongoRepository.rsqlQuery("metaData==(name=='license' and value=='CC BY-SA')")
        assert(result.size() == 1)
      }


    }
  }
}

@EnableMongoRepositories(basePackageClasses = Array(
  classOf[ISpectrumMongoRepositoryCustom]
), excludeFilters = Array())
@Configuration
class MyTestConfig extends AbstractMongoConfiguration{
  val server: String = scala.util.Properties.envOrElse("MONGO_SERVER", "127.0.0.1" )
  val database: String = "monatest"

  override def mongo(): Mongo = {
    new MongoClient(server)
  }

  override def getDatabaseName: String = {
    database
  }

}