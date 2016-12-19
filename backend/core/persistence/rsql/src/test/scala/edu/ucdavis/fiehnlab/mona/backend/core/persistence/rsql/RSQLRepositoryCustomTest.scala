package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql

import java.io.InputStreamReader
import java.lang.Iterable

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.JSONDomainReader
import org.scalatest.WordSpec
import org.springframework.data.domain.{Page, PageRequest}
import org.springframework.data.repository.CrudRepository
import org.springframework.test.context.TestContextManager

import scala.reflect.ClassTag
import scala.util.Properties

/**
  * Created by wohlg_000 on 3/9/2016.
  */
abstract class RSQLRepositoryCustomTest[T: ClassTag, Q] extends WordSpec with LazyLogging {

  val keepRunning = Properties.envOrElse("keep.server.running", "false").toBoolean


  //58 spectra for us to work with
  val exampleRecords: Array[T] = JSONDomainReader.create[Array[T]].read(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")))

  s"a repository is loaded with ${exampleRecords.length} compounds" when {

    "issues standard crud commands " should {

      "we should be able to reset our data" in {
        getRepository.deleteAll()
        assert(getRepository.count() == 0)
      }

      List(1, 2, 3).foreach { iteration =>

        s"we must be able to support doing several iterations of the process, this is iteration ${iteration} " must {
          s"we should be able to store our data" in {

            for (spectrum <- exampleRecords) {
              val size = getRepository.count()

              val result = getRepository.save(spectrum) //saveOrUpdate
              assert(result.isInstanceOf[T])

              val newSize = getRepository.count()

              assert(newSize == size + 1)
            }

          }

          s"we should have ${exampleRecords.length} records in the repository now" in {
            assert(getRepository.count() == 58)
          }


          "we should be able to execute RSQL queries like compound=q='inchiKey==GHSJKUNUIHUPDF-BYPYZUCNSA-N'" in {

            val result = getRepository.rsqlQuery(s"compound=q='inchiKey==GHSJKUNUIHUPDF-BYPYZUCNSA-N and kind==biological'")

            assert(result.size() == 1)
          }

          "we should be able to execute RSQL queries like compound.names.name=='META-HYDROXYBENZOIC ACID'" in {
            val result = getRepository.rsqlQuery(s""" compound=q='names.name=="META-HYDROXYBENZOIC ACID" ' """)
            assert(result.size() == 1)
          }

          "we should be able to execute RSQL queries like splash.block1=='splash10'" in {
            val result = getRepository.rsqlQuery(s"splash.block1==splash10")
            assert(result.size() == exampleRecords.length)
          }

          "we should be able to execute RSQL queries like splash.block1=='splash10' with pagination" in {
            val result: Page[T] = getRepository.rsqlQuery(s"splash.block1==splash10", new PageRequest(0, 10))

            assert(result.getContent.size() == 10)
            assert(result.getTotalPages == 6)
          }


          "we should be able query by id==\"3488925\"" in {
            val result = getRepository.rsqlQuery(s"id==3488925")
            assert(result.size() == 1)
          }

          "we should be able to execute RSQL queries like metaData=q='name==\"license\" and value==\"CC BY-SA\"'" in {
            val result = getRepository.rsqlQuery("metaData=q='name==license and value==\"CC BY-SA\"'")
            assert(result.size() == 58)
          }


          "we should be able to execute RSQL queries like compound=q=\'metaData=q='name==\"total exact mass\" and value=gt=306 and value=lt=307'\'" in {
            val result = getRepository.rsqlQuery("compound.metaData=q='name==\"total exact mass\" and value=gt=306 and value=lt=307'")
            assert(result.size == 2)
          }

          "we should be able to support subqueries in sub queries for compound" in {
            val result = getRepository.rsqlQuery("""compound=q='names.name=="META-HYDROXYBENZOIC ACID" and kind==biological and metaData=q="(name==\'total exact mass\')" '""")
          }

          "we should be able to execute RSQL queries like compound.metaData=q='name==\"total exact mass\" and value=gt=306.07 and value=lt=306.08'" in {
            val result = getRepository.rsqlQuery("compound.metaData=q='name==\"total exact mass\" and value=gt=306.07 and value=lt=306.08'")
            assert(result.size == 2)
          }

          "we should be able to execute RSQL queries like metaData=q='name=\"ion mode\" and value=negative' in " in {
            val result = getRepository.rsqlQuery("metaData=q='name==\"ion mode\" and value==negative'")
            assert(result.size == 25)
          }

          "we should be able to execute RSQL queries like metaData=q='name=match=\"ion.mode\" and value=negative' in " in {
            val result = getRepository.rsqlQuery("metaData=q='name=match=\"ion.mode\" and value==negative'")
            assert(result.size == 25)
          }

          "we should be able to execute RSQL queries like metaData=q='name=\"ion mode\" and value=match=negativ[ewq]' in " in {
            val result = getRepository.rsqlQuery("metaData=q='name==\"ion mode\" and value=match=negativ[ewq]'")
            assert(result.size == 25)
          }

          "we should be able to execute RSQL queries like tags=q='text==LCMS' in " in {
            val result = getRepository.rsqlQuery("tags=q='text==LCMS'")
            assert(result.size == 58)
          }

          "we should be able to execute RSQL queries like tags=q='text=match=\"[(LCMS)(lcms)]+\"' in " in {
            val result = getRepository.rsqlQuery("tags=q='text=match=\"[(LCMS)(lcms)]+\"'")
            assert(result.size == 58)
          }

          "readding the same events should be an update" in {
            val it = getRepository.findAll().iterator()

            while (it.hasNext) {
              getRepository.save(it.next()) //saveOrUpdate
            }

            assert(getRepository.count() == exampleRecords.length)
          }

          "retrieve all data" in {
            val result: Iterable[T] = getRepository.findAll()

            assert(getRepository.count() == exampleRecords.length)

            val it = result.iterator()
            assert(it.hasNext)
          }


          "if specified the server should stay online, this can be done using the env variable 'keep.server.running=true' " in {
            if (keepRunning) {
              while (keepRunning) {
                logger.warn("waiting forever till you kill me!")
                Thread.sleep(300000); // Every 5 minutes
              }
            }
          }

          "possible to delete one object" in {
            assert(getRepository.count() == exampleRecords.length)
            val one = getRepository.findAll().iterator().next()
            getRepository.delete(one)
            assert(exampleRecords.length - 1 == getRepository.count())
          }


          "possible to delete all data" in {
            getRepository.deleteAll()

            assert(0 == getRepository.count())
          }
        }
      }
    }
  }

  def getRepository: RSQLRepositoryCustom[T, Q] with CrudRepository[T, String]
}
