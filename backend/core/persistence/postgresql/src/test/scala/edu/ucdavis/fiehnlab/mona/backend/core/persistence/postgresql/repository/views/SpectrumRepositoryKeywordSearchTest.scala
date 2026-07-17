package edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.views

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.MonaMapper
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.postgresql.repository.SpectrumRepository
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.{ActiveProfiles, TestContextManager}

import java.io.InputStreamReader
import scala.jdk.CollectionConverters._

/**
 * Exercises the native keyword (contains) search backing the search box. The query resolves
 * matching spectrum ids per branch (spectrum metadata values, compound names, compound metadata
 * values) and UNIONs them, so these tests cover substring matching, case insensitivity,
 * deduplication across branches, pagination ordering and LIKE metacharacter escaping. The trigram
 * indexes only accelerate these queries in a real database, ILIKE works without them here
 */
@SpringBootTest
@ActiveProfiles(Array("test", "mona.persistence", "mona.persistence.init"))
class SpectrumRepositoryKeywordSearchTest extends AnyWordSpec with Matchers with LazyLogging {
  @Autowired
  val spectrumRepository: SpectrumRepository = null

  val monaMapper: ObjectMapper = MonaMapper.create

  val exampleRecords: Array[Spectrum] = monaMapper.readValue(new InputStreamReader(getClass.getResourceAsStream("/monaRecords.json")), new TypeReference[Array[Spectrum]] {})

  new TestContextManager(this.getClass).prepareTestInstance(this)

  "Keyword search" when {
    "the database is loaded with the example records" should {
      "start from an empty database" in {
        spectrumRepository.deleteAll()
        assert(spectrumRepository.count() == 0)
      }

      "load the example records" in {
        exampleRecords.foreach(spectrumRepository.save(_))
        assert(spectrumRepository.count() == 59)
      }

      "find spectra by a substring of a compound name" in {
        // 4 records carry a compound name containing hydroxybenzoic, e.g. META-HYDROXYBENZOIC ACID
        assert(spectrumRepository.countByKeyword("hydroxybenzoic") == 4)
        assert(spectrumRepository.findByKeyword("hydroxybenzoic", 0, 10).size() == 4)
      }

      "match case insensitively" in {
        assert(spectrumRepository.countByKeyword("HYDROXYBENZOIC") == 4)
        assert(spectrumRepository.countByKeyword("HydroxyBenzoic") == 4)
      }

      "find spectra by a substring of a metadata value" in {
        // 33 records carry an ion mode metadata value of positive
        assert(spectrumRepository.countByKeyword("positive") == 33)
      }

      "count each spectrum once even when it matches in several branches" in {
        // 58 records match acid through compound names and metadata values combined, many in both
        assert(spectrumRepository.countByKeyword("acid") == 58)
      }

      "agree between count and the total of all pages" in {
        val total = spectrumRepository.countByKeyword("acid")
        val all = spectrumRepository.findByKeyword("acid", 0, 100)
        assert(all.size() == total)
      }

      "return pages ordered by id descending without overlap" in {
        val all = spectrumRepository.findByKeyword("hydroxybenzoic", 0, 10).asScala.map(_.getId).toList
        val firstPage = spectrumRepository.findByKeyword("hydroxybenzoic", 0, 2).asScala.map(_.getId).toList
        val secondPage = spectrumRepository.findByKeyword("hydroxybenzoic", 1, 2).asScala.map(_.getId).toList

        assert(all == all.sorted.reverse)
        assert(firstPage ++ secondPage == all)
      }

      "return nothing for a keyword that matches no record" in {
        assert(spectrumRepository.countByKeyword("zzzznomatch") == 0)
        assert(spectrumRepository.findByKeyword("zzzznomatch", 0, 10).isEmpty)
      }

      "treat LIKE metacharacters as literals" in {
        // 58 records contain the substring 100, so an unescaped trailing % would match them all
        assert(spectrumRepository.countByKeyword("100%") == 0)
        assert(spectrumRepository.countByKeyword("acid_") == 0)
        assert(spectrumRepository.countByKeyword("acid\\") == 0)
      }
    }
  }
}
