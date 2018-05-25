package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import java.nio.file.{Files, Path}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.{Compound, MetaData, Spectrum}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.QueryExport

/**
  * Created by sajjan on 5/3/18.
  */
class IdentifierTableDownloader(export: QueryExport, downloadDir: Path, compress: Boolean = true) extends SpectrumDownloader(export, downloadDir, compress) {

  /**
    * Filename for this export
    *
    * @return
    */
  override def exportFilename: String = s"MoNA-export-$basename-identifier-table.csv"

  /**
    * File format prefix
    *
    * @return
    */
  override def getContentPrefix: String = ""

  /**
    * File format suffix
    *
    * @return
    */
  override def getContentSuffix: String = ""

  /**
    * File format separator
    *
    * @return
    */
  override def getRecordSeparator: String = ""


  /**
    * Create description file and prevent writing query file
    */
  override def writeAssociatedFiles(): Unit = {
    val descriptionFile: Path =
      if (compress)
        downloadDir.resolve(compressedExportFilename + ".description.txt")
      else
        downloadDir.resolve(exportFilename + ".description.txt")

    Files.write(descriptionFile, "Table spectral and chemical identifiers for all MoNA records".getBytes)
  }


  /**
    *
    */
  override def writeSpectrum(spectrum: Spectrum): Unit = {
    val sb: StringBuilder = new StringBuilder
    sb.append(spectrum.id).append(',')

    // Add SPLASH
    if (spectrum.splash != null) {
      sb.append(spectrum.splash.splash)
    }
    sb.append(',')

    // Get compound and structures
    if (spectrum.compound != null && spectrum.compound.nonEmpty) {
      // Add InChIKey
      val compound: Compound = spectrum.compound.find(_.kind == "biological").getOrElse(spectrum.compound.head)

      if (compound.inchiKey != null && compound.inchiKey.nonEmpty) {
        sb.append(compound.inchiKey).append(',')
      } else {
        val metaData: Option[MetaData] = compound.metaData.find(_.name == "InChIKey")

        if (metaData.isDefined) {
          sb.append(metaData.get.value).append(',')
        }
      }

      // Add SMILES
      val metaData: Option[MetaData] = compound.metaData.find(_.name.toLowerCase == "smiles")

      if (metaData.isDefined) {
        sb.append(metaData.get.value)
      }
    } else {
      sb.append(',')
    }

    exportWriter.write(sb.append('\n').toString())
  }
}
