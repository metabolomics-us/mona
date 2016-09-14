package edu.ucdavis.fiehnlab.mona.backend.services.downloader.types

import java.util.Date

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field


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

@Document(collection = "PREDEFINED_QUERIES")
case class PredefinedQuery(
                            @(Id@field)
                            label: String,
                            description: String,

                            query: String,
                            queryCount: Long,

                            jsonExport: QueryExport,
                            mspExport: QueryExport
                          )