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
                               values: Array[MetaDataValueCount]
                             )

case class MetaDataValueCount(
                        value: String,
                        count: Int
                        )

@Document(collection = "STATISTICS_TAGS")
case class TagStatistics(
                           @(Id@field)
                           text: String,
                           ruleBased: Boolean,
                           count: Int
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