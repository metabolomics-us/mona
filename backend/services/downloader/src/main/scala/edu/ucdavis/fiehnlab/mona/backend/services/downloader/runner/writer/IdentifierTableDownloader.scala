package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.writer

import java.nio.file.{Files, Path}
import edu.ucdavis.fiehnlab.mona.backend.services.downloader.domain.QueryExport
import edu.ucdavis.fiehnlab.mona.backend.core.domain.dao.{CompoundDAO, MetaDataDAO, Spectrum}

import scala.jdk.CollectionConverters._

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

    Files.write(descriptionFile, "Table of spectral and chemical identifiers for all MoNA records".getBytes)
  }


  /**
    *
    */
  override def writeSpectrum(spectrum: Spectrum): Unit = {
    val sb: StringBuilder = new StringBuilder
    sb.append(spectrum.getId).append(',')

    // Add SPLASH
    if (spectrum.getSplash != null) {
      sb.append(spectrum.getSplash.getSplash)
    }
    sb.append(',')

    // Get compound and structures
    if (spectrum.getCompound != null && spectrum.getCompound.asScala.nonEmpty) {
      // Add InChIKey
      val compound: CompoundDAO = spectrum.getCompound.asScala.find(_.getKind == "biological").getOrElse(spectrum.getCompound.asScala.head)

      if (compound.getInchiKey != null && compound.getInchiKey.nonEmpty) {
        sb.append(compound.getInchiKey).append(',')
      } else {
        val metaData: Option[MetaDataDAO] = compound.getMetaData.asScala.find(_.getName == "InChIKey")

        if (metaData.isDefined) {
          sb.append(metaData.get.getValue).append(',')
        }
      }

      // Add SMILES
      val metaData: Option[MetaDataDAO] = compound.getMetaData.asScala.find(_.getName.toLowerCase == "smiles")

      if (metaData.isDefined) {
        sb.append(metaData.get.getValue)
      }
    } else {
      sb.append(',')
    }

    exportWriter.write(sb.append('\n').toString())
  }
}
