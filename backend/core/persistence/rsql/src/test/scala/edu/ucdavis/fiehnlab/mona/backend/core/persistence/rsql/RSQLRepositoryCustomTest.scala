package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql

import java.io.{File, FileReader}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.{Splash, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.{WordSpec, FunSuite}
import org.springframework.data.domain.{PageRequest, Page}
import org.springframework.data.repository.CrudRepository
import org.springframework.test.context.TestContextManager

import scala.reflect.ClassTag

/**
  * Created by wohlg_000 on 3/9/2016.
  */
abstract class RSQLRepositoryCustomTest[T:ClassTag] extends WordSpec {


  //required for spring and scala tes
  new TestContextManager(this.getClass()).prepareTestInstance(this)

  "a repository is loaded with 58 compounds" when {


    assert(getRepository != null)

    //58 spectra for us to work with
    val exampleRecords: Array[T] = JSONDomainReader.create[Array[T]].read(new FileReader(new File("src/test/resources/monaRecords.json")))


    "reading our records" should {
      s"results in ${exampleRecords.length} records" in {
        assert(exampleRecords.length == 58)
      }
    }

    "issues standard crud commands " should {

      getRepository.deleteAll()
      assert(getRepository.count() == 0)

      for (spectrum <- exampleRecords) {
        val size = getRepository.count()

        getRepository.save(spectrum)
        val newSize = getRepository.count()

        assert(newSize == size + 1)
      }

      "we should be able to execute RSQL queries like biologicalCompound.inchiKey==GHSJKUNUIHUPDF-BYPYZUCNSA-N" in {

        val result = getRepository.rsqlQuery(s"biologicalCompound.inchiKey==GHSJKUNUIHUPDF-BYPYZUCNSA-N")

        assert(result.size() == 1)
      }

      "we should be able to execute RSQL queries like biologicalCompound.names.name=='META-HYDROXYBENZOIC ACID'" in {
        val result = getRepository.rsqlQuery(s"biologicalCompound.names.name=='META-HYDROXYBENZOIC ACID'")
        assert(result.size() == 1)
      }

      "we should be able to execute RSQL queries like metaData=q='name==\"license\" and value==\"CC BY-SA\"'" in {
        val result = getRepository.rsqlQuery("metaData=q='name==\"license\" and value==\"CC BY-SA\"'")
        assert(result.size() == 58)
      }

      "we should be able to execute RSQL queries like chemicalCompound.metaData=q='name==\"total exact mass\" and value=gt=306 and value=lt=307'" in {
        val result = getRepository.rsqlQuery("chemicalCompound.metaData=q='name==\"total exact mass\" and value=gt=306 and value=lt=307'")
        assert(result.size == 2)
      }

      "we should be able to execute RSQL queries like chemicalCompound.metaData=q='name==\"total exact mass\" and value=gt=306.07 and value=lt=306.08'" in {
        val result = getRepository.rsqlQuery("chemicalCompound.metaData=q='name==\"total exact mass\" and value=gt=306.07 and value=lt=306.08'")
        assert(result.size == 2)
      }
    }
  }

  def getRepository: RSQLRepositoryCustom[T] with CrudRepository[T, String]
}
