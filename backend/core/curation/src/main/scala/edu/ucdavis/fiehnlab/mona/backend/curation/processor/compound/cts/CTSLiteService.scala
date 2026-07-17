package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations

/**
  * Shared client for CTS-Lite, the replacement for the retired Chemical Translation Service. CTS-Lite cannot
  * return a MOL or accept a name, but it translates a valid InChIKey to an InChI, SMILES and a compound name
  * which lets us resolve a structure locally with the CDK and backfill a missing compound name
  */
@Service
class CTSLiteService extends LazyLogging {

  val CTS_LITE_URL: String = "https://cts-lite.metabolomics.us/match"

  // Standard InChIKey layout: 14 letters, 10 letters and a final letter, separated by dashes
  val INCHIKEY_PATTERN: String = CTSLiteService.INCHIKEY_PATTERN

  @Autowired
  protected val restOperations: RestOperations = null

  /**
    * Look up the first match for a syntactically valid InChIKey, returning None when the key is missing,
    * malformed, has no match, or CTS-Lite is unavailable
    *
    * @param inchikey
    * @param id
    * @return
    */
  def matchInChIKey(inchikey: String, id: String): Option[CTSLiteMatch] = {
    if (inchikey == null || inchikey.isEmpty) {
      logger.info(s"$id: No InChIKey provided for CTS-Lite lookup")
      None
    } else if (!inchikey.matches(INCHIKEY_PATTERN)) {
      logger.info(s"$id: Skipping CTS-Lite lookup, '$inchikey' is not a valid InChIKey")
      None
    } else {
      logger.info(s"$id: Querying CTS-Lite for $inchikey, invoking url $CTS_LITE_URL")

      try {
        val response: ResponseEntity[Array[CTSLiteResult]] =
          restOperations.postForEntity(CTS_LITE_URL, CTSLiteRequest(inchikey), classOf[Array[CTSLiteResult]])

        Option(response.getBody)
          .flatMap(_.headOption)
          .filter(_.found_match)
          .flatMap(result => Option(result.matches))
          .flatMap(_.headOption)
      } catch {
        case e: Throwable =>
          logger.error(s"$id: Error during CTS-Lite lookup: ${e.getMessage}")
          None
      }
    }
  }
}

object CTSLiteService {
  // Standard InChIKey layout: 14 letters, 10 letters and a final letter, separated by dashes
  val INCHIKEY_PATTERN: String = "[A-Z]{14}-[A-Z]{10}-[A-Z]"
}

case class CTSLiteRequest(queries: String)

// Field names match the CTS-Lite snake_case JSON so Jackson can bind them directly
case class CTSLiteMatch(inchikey: String, inchi: String, smiles: String, compound_name: String)

case class CTSLiteResult(found_match: Boolean, match_level: String, matches: Array[CTSLiteMatch], error_message: String)
