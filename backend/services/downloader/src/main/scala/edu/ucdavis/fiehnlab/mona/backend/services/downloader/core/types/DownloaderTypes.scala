package edu.ucdavis.fiehnlab.mona.backend.services.downloader.core.types

import java.util.Date

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field


/**
  * Query export corresponding to a single exported results file format
  * @param id
  * @param label
  * @param query
  * @param format
  * @param emailAddress
  * @param date
  * @param count
  * @param size
  * @param queryFile
  * @param exportFile
  */
@Document(collection = "QUERY_EXPORTS")
case class QueryExport(
                        @(Id@field)
                        id: String,

                        label: String,
                        query: String,
                        format: String,
                        emailAddress: String,

                        date: Date,
                        count: Long,
                        size: Long,

                        queryFile: String,
                        exportFile: String
                      )

/**
  * Definition for an individual query download with multiple formats
  * @param label
  * @param description
  * @param query
  * @param queryCount
  * @param jsonExport
  * @param mspExport
  * @param sdfExport
  */
@Document(collection = "PREDEFINED_QUERIES")
case class PredefinedQuery(
                            @(Id@field)
                            label: String,
                            description: String,

                            query: String,
                            queryCount: Long,

                            jsonExport: QueryExport,
                            mspExport: QueryExport,
                            sdfExport: QueryExport
                          )


/**
  * Summary for a static download
  * @param fileName
  * @param category
  * @param description
  */
case class StaticDownload(
                           fileName: String,
                           category: String,
                           description: String
                         )

object StaticDownload {
  def apply(filePath: String): StaticDownload = apply(filePath, null)

  def apply(filePath: String, description: String): StaticDownload = {
    val path: Array[String] = filePath.split('/')

    if (path.length == 1) {
      StaticDownload(filePath, null, description)
    } else {
      StaticDownload(path.last, path.head, description)
    }
  }
}