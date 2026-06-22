package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Names, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations.Step
import edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts.CTSLiteService
import edu.ucdavis.fiehnlab.mona.backend.curation.util.CommonMetaData
import org.springframework.batch.item.ItemProcessor
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.mutable.Buffer
import scala.jdk.CollectionConverters._

/**
  * Backfills a compound name from CTS-Lite when none was provided, covering the case where a spectrum was
  * uploaded with only a structure or InChIKey. The name is stored as computed so RemoveComputedData strips it
  * at the start of the next curation run and it is looked up again, mirroring the "Update Compound" button
  */
@Step(description = "this step resolves a missing compound name from CTS-Lite using the InChIKey", workflow = "spectra-curation")
class ResolveCompoundNames extends ItemProcessor[Spectrum, Spectrum] with LazyLogging {

  @Autowired
  protected val ctsLiteService: CTSLiteService = null

  override def process(spectrum: Spectrum): Spectrum = {
    val updatedCompound: Buffer[Compound] = spectrum.getCompound.asScala.map(compound => resolveName(compound, spectrum.getId))
    spectrum.setCompound(updatedCompound.asJava)
    spectrum
  }

  def resolveName(compound: Compound, id: String): Compound = {
    val existingNames: Buffer[Names] =
      if (compound.getNames != null) compound.getNames.asScala else Buffer[Names]()

    // Only look up a name when none was provided. Computed names are stripped before this step runs,
    // so any remaining name with a value is one the submitter supplied
    val hasName: Boolean = existingNames.exists(n => n.getName != null && n.getName.trim.nonEmpty)

    if (hasName) {
      compound
    } else {
      // Prefer the computed InChIKey, falling back to the submitted one
      val inchikey: String = compound.getMetaData.asScala
        .find(x => x.getName == CommonMetaData.INCHI_KEY && x.getComputed)
        .map(_.getValue.toString)
        .orElse(Option(compound.getInchiKey))
        .orNull

      ctsLiteService.matchInChIKey(inchikey, id) match {
        case Some(matched) if matched.compound_name != null && matched.compound_name.trim.nonEmpty =>
          logger.info(s"$id: Resolved compound name '${matched.compound_name}' from CTS-Lite")
          val resolved = new Names(true, matched.compound_name, null, "CTS-Lite")
          compound.setNames((existingNames :+ resolved).asJava)
          compound

        case _ =>
          logger.info(s"$id: No compound name resolved from CTS-Lite")
          compound
      }
    }
  }
}
