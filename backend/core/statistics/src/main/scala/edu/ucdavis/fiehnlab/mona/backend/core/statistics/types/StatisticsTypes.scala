package edu.ucdavis.fiehnlab.mona.backend.core.statistics.types

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
                               values: Array[(String, Int)]
                             )

@Document(collection = "STATISTICS_TAGS")
case class TagStatistics(
                           @(Id@field)
                           text: String,
                           count: Int
                         )