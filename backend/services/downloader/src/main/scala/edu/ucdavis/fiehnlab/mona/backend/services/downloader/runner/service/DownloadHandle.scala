package edu.ucdavis.fiehnlab.mona.backend.services.downloader.runner.service

import java.util.{Date, UUID}

import edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types.{PredefinedQuery, QueryExport}

class DownloadHandle(export: QueryExport, static: Boolean = false) {

  val basename: String = DownloadHandle.buildExportBasename(export.label, export.emailAddress)

  val queryFilename: String = s"MoNA-export-$basename-query.txt"
  val exportFilename: String = s"MoNA-export-$basename.${export.format}"
  val compressedExportFilename: String = s"MoNA-export-$basename-${export.format}.zip"

}

object DownloadHandle {

  /**
    *
    * @param export
    * @return
    */
  private def buildExportBasename(export: QueryExport): String = {
    // Use the export ID as the label if one does not exist
    val label: String =
      if (export.label == null || export.label.isEmpty)
        export.id
      else
        export.label

    val sanitizedLabel: String = label.split(" - ").last.replaceAll(" ", "_").replaceAll("/", "-")

    if (export.emailAddress == null || export.emailAddress.isEmpty) {
      sanitizedLabel
    } else {
      export.emailAddress.split("@").head + '-' + sanitizedLabel
    }
  }

  /**
    * Create new QueryExport objects if it is currently null
    * @param query
    * @param export
    * @param format
    * @param static
    * @return
    */
  private def createDownloadHandle(query: PredefinedQuery, export: QueryExport, format: String, static: Boolean = false): DownloadHandle = export match {

    case null =>
      val export = QueryExport(UUID.randomUUID.toString, query.label, query.query, format, null, new Date, 0, 0, null, null)
      new DownloadHandle(export, static)

    case export: QueryExport =>  new DownloadHandle(export, static)
  }

  /**
    * Create a download handle from a predefined query given the export format
    * @param predefinedQuery
    * @param format
    * @param static
    * @return
    */
  def apply(predefinedQuery: PredefinedQuery, format: String, static: Boolean = false): DownloadHandle = format match {
    case "json" => createDownloadHandle(predefinedQuery, predefinedQuery.jsonExport, format, static)
    case "msp" => createDownloadHandle(predefinedQuery, predefinedQuery.mspExport, format, static)
    case "sdf" => createDownloadHandle(predefinedQuery, predefinedQuery.sdfExport, format, static)
    case _ => createDownloadHandle(predefinedQuery, null, format, static)
  }
}