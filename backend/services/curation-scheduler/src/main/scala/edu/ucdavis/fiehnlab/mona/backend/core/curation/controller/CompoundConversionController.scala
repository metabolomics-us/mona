package edu.ucdavis.fiehnlab.mona.backend.core.curation.controller

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.curation.service.{CompoundConversionService, CompoundSummary}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.WrappedString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation.{RequestBody, RequestMapping, RequestMethod, RestController}

@RestController
@RequestMapping(value = Array("/rest/conversion"))
class CompoundConversionController extends LazyLogging {

  @Autowired
  val compoundConversionService: CompoundConversionService = null

  @RequestMapping(path = Array("/smiles"), method = Array(RequestMethod.POST))
  def parseSmiles(@RequestBody smiles: WrappedString): ResponseEntity[CompoundSummary] = {
    try {
      new ResponseEntity[CompoundSummary](
        compoundConversionService.parseSmiles(smiles.string), HttpStatus.OK)
    } catch {
      case e: Exception =>
        logger.error(s"Unable to parse SMILES ${smiles.string}")
        new ResponseEntity[CompoundSummary](HttpStatus.INTERNAL_SERVER_ERROR)
    }
  }

  @RequestMapping(path = Array("/inchi"), method = Array(RequestMethod.POST))
  def parseInChI(@RequestBody inchi: WrappedString): ResponseEntity[CompoundSummary] = {
    try {
      new ResponseEntity[CompoundSummary](
        compoundConversionService.parseInChI(inchi.string), HttpStatus.OK)
    } catch {
      case e: Exception =>
        logger.error(s"Unable to parse InChI ${inchi.string}")
        new ResponseEntity[CompoundSummary](HttpStatus.INTERNAL_SERVER_ERROR)
    }
  }

  @RequestMapping(path = Array("/mol"), method = Array(RequestMethod.POST))
  def parseMol(@RequestBody mol: WrappedString): ResponseEntity[CompoundSummary] = {
    try {
      new ResponseEntity[CompoundSummary](
        compoundConversionService.parseMol(mol.string), HttpStatus.OK)
    } catch {
      case e: Exception =>
        logger.error(s"Unable to parse MOL ${mol.string}")
        new ResponseEntity[CompoundSummary](HttpStatus.INTERNAL_SERVER_ERROR)
    }
  }
}

