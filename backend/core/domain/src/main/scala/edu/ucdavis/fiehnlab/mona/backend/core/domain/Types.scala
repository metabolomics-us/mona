package edu.ucdavis.fiehnlab.mona.backend.core.domain

import com.fasterxml.jackson.annotation.{JsonIgnore, JsonProperty}
import com.fasterxml.jackson.databind.annotation.{JsonSerialize, JsonDeserialize}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.annotation.{TupleSerialize, CascadeSave}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.{NumberDeserializer}
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.{DBRef, Document}

import scala.annotation.meta.field
import scala.beans.{BeanInfo, BeanProperty}
import org.springframework.data.elasticsearch.annotations.{Mapping, FieldIndex, FieldType, Field}
import org.springframework.data.elasticsearch.annotations.NestedField._

/**
  * definition of the MoNA domain classes
  * TODO: make DBRefs work with cascade save
  */

object Types {

  case class MetaData(

                       @(Indexed@field)
                       category: String,

                       @(Indexed@field)
                       computed: Boolean,

                       @(Indexed@field)
                       hidden: Boolean,

                       @(Indexed@field)
                       @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                       name: String,

                       score: Score,

                       @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                       @(Indexed@field)
                       unit: String,

                       @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                       url: String,

                       @(TupleSerialize@field)
                       @JsonDeserialize(using = classOf[NumberDeserializer])
                       value: Any

                     )

  case class Names(

                    @(Indexed@field)
                    @(Field@field)(`type` = FieldType.Boolean, index = FieldIndex.not_analyzed)
                    computed: Boolean,

                    @(Indexed@field)
                    @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                    name: String,

                    score: Double,

                    @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                    source: String
                  )


  case class Tags(
                   @(Indexed@field)
                   ruleBased: Boolean,

                   @(Indexed@field)
                   @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                   text: String
                 )


  case class Compound(

                       @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                       inchi: String,
                       @(Indexed@field)
                       @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                       inchiKey: String,

                       @(Field@field)(`type` = FieldType.Nested)
                       metaData: Array[MetaData],

                       molFile: String,

                       @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                       names: Array[Names],

                       @(Field@field)(`type` = FieldType.Nested)
                       tags: Array[Tags]
                     )


  case class Impacts(

                      impactValue: Double,

                      reason: String
                    )


  case class Score(

                    impacts: Array[Impacts],

                    relativeScore: Double, //ns

                    scaledScore: Double, //ns

                    score: Double
                  )


  case class Splash(

                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     block1: String, //ns

                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     block2: String, //ns

                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     block3: String, //ns

                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     @(Indexed@field)
                     splash: String
                   )


  @Document(collection = "SUBMITTER")
  case class Submitter(

                        emailAddress: String,

                        firstName: String,

                        institution: String,

                        lastName: String
                      )

  case class Author(
                     @(Indexed@field)

                     emailAddress: String,
                     @(Indexed@field)

                     firstName: String,
                     @(Indexed@field)

                     institution: String,
                     @(Indexed@field)

                     lastName: String
                   )

  //this is way to uggly, we might really need to use DAO's :(
  @Document(collection = "SPECTRUM")
  @org.springframework.data.elasticsearch.annotations.Document(indexName = "spectrum", `type` = "spectrum", shards = 1, replicas = 0, refreshInterval = "-1")
  case class Spectrum(

                       @(Field@field)(`type` = FieldType.Object)
                       biologicalCompound: Compound,
                       @(Field@field)(`type` = FieldType.Object)
                       chemicalCompound: Compound,
                       @(Field@field)(`type` = FieldType.Object)
                       predictedCompound: Compound,

                       @(Id@field)
                       id: String,

                       lastUpdated: String,

                       @(Field@field)(`type` = FieldType.Nested)
                       metaData: Array[MetaData],

                       @(Field@field)(`type` = FieldType.Object)
                       score: Score,

                       @(Field@field)(`type` = FieldType.String)
                       spectrum: String,

                       @(Field@field)(`type` = FieldType.Object)
                       splash: Splash,

                       @(Field@field)(`type` = FieldType.Object)
                       submitter: Submitter,

                       @(Field@field)(`type` = FieldType.Nested)
                       tags: Array[Tags],

                       @(Field@field)(`type` = FieldType.Nested)
                       authors: Array[Author]
                     )

}

/**
  * makes serializations simpler
  */
object HelperTypes {

  /**
    * a simple query
    *
    * @param string
    */
  case class WrappedString(string: String)

}