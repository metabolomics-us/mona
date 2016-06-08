package edu.ucdavis.fiehnlab.mona.backend.services.downloader

import java.util.Date

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field


@Document(collection = "QUERY_EXPORTS")
case class QueryExport(
                        @(Id@field)
                        label: String,
                        query: String,
                        format: String,
                        count: Integer,
                        size: Long,
                        emailAddress: String
                      )

@Document(collection = "QUERY_DOWNLOAD_REQUESTS")
case class QueryDownloadRequest(
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