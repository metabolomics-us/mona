package edu.ucdavis.fiehnlab.mona.backend.core.statistics.types

import java.util.Date

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field

/**
  * Created by sajjan on 8/2/16.
  */
@Document(collection = "STATISTICS_METADATA")
case class MetaDataStatistics(
                               @(Id@field)
                               name: String,
                               count: Int,
                               values: Array[MetaDataValueCount]
                             )

case class MetaDataStatisticsSummary(
                                      @(Id@field)
                                      name: String,
                                      count: Int
                                    )

case class MetaDataValueCount(
                               value: String,
                               count: Int
                             )

@Document(collection = "STATISTICS_TAGS")
case class TagStatistics(
                          @(Id@field)
                          id: String,
                          text: String,
                          ruleBased: Boolean,
                          count: Int,
                          category: String
                        )

@Document(collection = "STATISTICS")
case class GlobalStatistics(
                             @(Id@field)
                             id: String,
                             date: Date,
                             spectrumCount: Long,
                             compoundCount: Long,
                             metaDataCount: Long,
                             metaDataValueCount: Long,
                             tagCount: Long,
                             tagValueCount: Long,
                             submitterCount: Long
                           )

@Document(collection = "STATISTICS_COMPOUNDCLASS")
case class CompoundClassStatistics(
                                    @(Id@field)
                                    name: String,
                                    spectrumCount: Int,
                                    compoundCount: Int
                                  )

@Document(collection = "STATISTICS_SUBMITTER")
case class SubmitterStatistics(
                                @(Id@field)
                                id: String,
                                firstName: String,
                                lastName: String,
                                institution: String,
                                count: Int,
                                score: Double
                              )
