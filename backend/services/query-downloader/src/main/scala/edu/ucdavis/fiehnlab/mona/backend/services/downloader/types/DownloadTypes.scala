package edu.ucdavis.fiehnlab.mona.backend.services.downloader.types

import java.util.Date

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field


@Document(collection = "QUERY_EXPORTS")
case class QueryExport(
                        @(Id@field)
                        label: String,
                        emailAddress: String,
                        queryFile: String,
                        queryCount: Integer,
                        exportFile: String,
                        exportSize: Long,
                        query: String
                      )

@Document(collection = "QUERY_DOWNLOADS")
case class QueryDownload(
                        date: Date,
                        label: String
                        )

@Document(collection = "PREDEFINED_QUERIES")
case class PredefinedQuery(
                          @(Id@field)
                          label: String,
                          query: String,
                          description: String,
                          queryCount: Integer,
                          jsonExport: QueryExport,
                          mspExport: QueryExport
                          )