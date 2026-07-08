package edu.ucdavis.fiehnlab.mona.backend.core.domain.io.upload

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, Library, MetaData, Names, Spectrum, SpectrumSubmitter, Tag}

import java.util.Date
import scala.jdk.CollectionConverters._

/**
  * Ports upload-library.service.ts's processData (origin tagging), MetadataOptimization's
  * optimizeMetaData (name/category normalization) and submitSpectrum/buildSpectrum (the final
  * Spectrum shape) into a single server side enrichment step, so a raw parsed spectrum becomes
  * exactly the Spectrum the interactive upload path would have produced for the same file.
  *
  * Note: the client's workOnSpectra only skips submission when inchiKey/inchi are literally
  * null, but none of the three parsers ever assign that literal (they leave the fields
  * undefined, or '' for MassBank's inchi), so in practice every parsed spectrum is submitted
  * regardless of whether it carries a structure identifier. This intentionally matches that behavior 
  */
object UploadSpectrumBuilder {

  private def optimize(entries: List[RawMetaEntry]): List[RawMetaEntry] = {
    entries.flatMap { entry =>
      if (entry.name == null || entry.name.trim.isEmpty) {
        None
      } else {
        Some(entry.copy(
          name = entry.name.replace('_', ' ').toLowerCase,
          category = entry.category.map(_.replace('_', ' ').toLowerCase)
        ))
      }
    }
  }

  /**
    * Builds a persistable Spectrum from a raw parsed spectrum, tagging it with the upload's
    * origin filename and attributing it to the given submitter, id, tags and, when present, library
    */
  def build(raw: RawParsedSpectrum, originFileName: String, submitter: SpectrumSubmitter,
            id: Option[String] = None, additionalTags: List[String] = Nil,
            library: Option[Library] = None): Spectrum = {

    val taggedMeta = raw.meta :+ RawMetaEntry("origin", originFileName)
    val optimizedMeta = optimize(taggedMeta)

    val libraryTag = library.map(l => new Tag(l.getLibrary, false)).toList
    val tags: List[Tag] = libraryTag ++ additionalTags.map(t => new Tag(t, false))

    val metaData: List[MetaData] = optimizedMeta.map { entry =>
      new MetaData(entry.name, entry.value, false, entry.category.orNull, false)
    }

    val compoundMetaData: List[MetaData] = raw.smiles.filter(_.nonEmpty).map { smiles =>
      new MetaData("SMILES", smiles, false, "none", false)
    }.toList

    val names: List[Names] = (raw.name.toList ++ raw.names)
      .filter(n => n != null && n.nonEmpty)
      .distinct
      .map(n => new Names(false, n, null, null))

    val compound = new Compound(
      "biological",
      new java.util.ArrayList[Tag](),
      raw.inchi.orNull,
      names.asJava,
      null,
      false,
      raw.inchiKey.orNull,
      compoundMetaData.asJava,
      new java.util.ArrayList[MetaData]()
    )

    new Spectrum(
      List(compound).asJava,
      id.orNull,
      metaData.asJava,
      new java.util.ArrayList[MetaData](),
      null,
      raw.spectrum,
      new Date(),
      new Date(),
      null,
      null,
      submitter,
      tags.asJava,
      library.orNull
    )
  }
}
