package edu.ucdavis.fiehnlab.mona.backend.core.domain

import java.util.Date
import javax.validation.constraints._

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import edu.ucdavis.fiehnlab.mona.backend.core.domain.annotation.{AnalyzedStringSerialize, TupleSerialize}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.NumberDeserializer
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.{Field, FieldIndex, FieldType}
import org.springframework.data.mongodb.core.index.{Indexed, TextIndexed}
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field
import scala.beans.BeanProperty

/**
  * definition of the MoNA domain classes and accepts arbitrary values, which needs to be supported by different
  * serializers
  */

case class MetaData(
                     @(Indexed@field)
                     category: String,

                     @(Indexed@field)
                     computed: Boolean,

                     @(Indexed@field)
                     hidden: Boolean,

                     @(Indexed@field)
                     @(TextIndexed@field)
                     @(AnalyzedStringSerialize@field)
                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     name: String,

                     score: Score,

                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     @(Indexed@field)
                     unit: String,

                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     url: String,

                     /**
                       * should be limited to the following classes
                       *
                       * String
                       * Integer
                       * Double
                       * Boolean
                       */
                     @(TupleSerialize@field)
                     @(TextIndexed@field)
                     @(JsonDeserialize@field)(using = classOf[NumberDeserializer])
                     value: Any
                   )

case class Names(
                  @(Indexed@field)
                  @(Field@field)(`type` = FieldType.Boolean, index = FieldIndex.not_analyzed)
                  computed: Boolean,

                  @(Indexed@field)
                  @(TextIndexed@field)
                  @(AnalyzedStringSerialize@field)
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
                 @(TextIndexed@field)
                 @(AnalyzedStringSerialize@field)
                 @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                 text: String
               )


/**
  * this defines a compound in the system, which can be computed or provided
  *
  * @param inchi
  * @param inchiKey
  * @param metaData
  * @param molFile
  * @param names
  * @param tags
  * @param computed
  * @param score
  * @param kind
  * @param classification
  */
case class Compound(
                     @Deprecated
                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     inchi: String,

                     @Deprecated
                     @(Indexed@field)
                     @(TextIndexed@field)
                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     inchiKey: String,

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     metaData: Array[MetaData],

                     molFile: String,

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     names: Array[Names],

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     tags: Array[Tags],

                     @(Indexed@field)
                     computed: Boolean = false,

                     @(Field@field)(`type` = FieldType.Object)
                     score: Score,

                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     kind: String = "biological",

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     classification: Array[MetaData] = Array()
                   )

case class Impact(value: Double, reason: String)

case class Score(
                  impacts: Array[Impact],

                  @(Indexed@field)
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
                   block4: String, //ns

                   @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                   @(Indexed@field)
                   @(TextIndexed@field)
                   splash: String
                 )


/**
  * defines the submitter for a spectrum an is basically the person who uploaded it
  * to the system
  *
  * @param id an internal id
  * @param emailAddress
  * @param firstName
  * @param lastName
  * @param institution
  */
@Document(collection = "SUBMITTER")
case class Submitter(
                      /**
                        * primary id for the user, can be any string
                        */
                      @(Id@field)
                      @(Indexed@field)
                      @(TextIndexed@field)
                      @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                      id: String,

                      @(Indexed@field)
                      @(TextIndexed@field)
                      @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                      emailAddress: String,

                      @(Indexed@field)
                      @(TextIndexed@field)
                      @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                      firstName: String,

                      @(Indexed@field)
                      @(TextIndexed@field)
                      @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                      lastName: String,

                      @(Indexed@field)
                      @(TextIndexed@field)
                      @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                      institution: String
                    )

/**
  * this defines an author, which actually acquired the spectra
  *
  * @param emailAddress
  * @param firstName
  * @param institution
  * @param lastName
  */
case class Author(
                   @(Indexed@field)
                   @(TextIndexed@field)
                   emailAddress: String,

                   @(Indexed@field)
                   @(TextIndexed@field)
                   firstName: String,

                   @(Indexed@field)
                   @(TextIndexed@field)
                   institution: String,

                   @(Indexed@field)
                   @(TextIndexed@field)
                   lastName: String
                 )

/**
  * this is the actual definition of a persistent spectrum in the MoNA database system
  *
  * @param id
  * @param lastUpdated
  * @param metaData
  * @param score
  * @param spectrum
  * @param splash
  * @param submitter
  * @param tags
  * @param authors
  */
@Document(collection = "SPECTRUM")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "spectrum", `type` = "spectrum", shards = 15)
case class Spectrum(
                     @(NotNull@field)
                     @(Size@field)(min = 1)
                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     compound: Array[Compound],

                     @(Id@field)
                     @(TextIndexed@field)
                     @(Size@field)(min = 1)
                     @BeanProperty
                     id: String,

                     dateCreated: Date,
                     lastUpdated: Date,

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     metaData: Array[MetaData],

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     annotations: Array[MetaData],

                     @(Field@field)(`type` = FieldType.Object)
                     score: Score,

                     @(NotNull@field)
                     @(NotEmpty@field)
                     @(Field@field)(`type` = FieldType.String)
                     spectrum: String,

                     @(Field@field)(`type` = FieldType.Object)
                     splash: Splash,

                     @(NotNull@field)
                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     submitter: Submitter,

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     tags: Array[Tags],

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     authors: Array[Author],

                     @(Field@field)(`type` = FieldType.Object)
                     library: Library
                   )

object Spectrum {

  /**
    * a simple way to generate a spectrum from a MoNA legacy spectrum
    *
    * @param spectrum
    * @return
    */
  def apply(spectrum: LegacySpectrum): Spectrum = {

    var compounds = List[Compound]()

    if (spectrum.biologicalCompound != null) {
      compounds = spectrum.biologicalCompound.copy(kind = "biological") :: compounds
    }

    if (spectrum.chemicalCompound != null) {
      compounds = spectrum.chemicalCompound.copy(kind = "observed") :: compounds
    }

    new Spectrum(
      spectrum = spectrum.spectrum,
      score = spectrum.score,
      id = spectrum.id,
      dateCreated = spectrum.dateCreated,
      lastUpdated = spectrum.lastUpdated,
      metaData = spectrum.metaData.filter(_.category != "annotation"),
      annotations = spectrum.metaData.filter(_.category == "annotation"),
      submitter = spectrum.submitter,
      tags = spectrum.tags,
      library = spectrum.library,
      authors = spectrum.authors,
      compound = compounds.toArray,
      splash = spectrum.splash
    )
  }
}

/**
  * old style mona spectra
  *
  * @param biologicalCompound
  * @param chemicalCompound
  * @param id
  * @param lastUpdated
  * @param metaData
  * @param score
  * @param spectrum
  * @param splash
  * @param submitter
  * @param tags
  * @param authors
  */
case class LegacySpectrum(
                           biologicalCompound: Compound,
                           chemicalCompound: Compound,
                           id: String,
                           dateCreated: Date,
                           lastUpdated: Date,
                           metaData: Array[MetaData],
                           score: Score,
                           spectrum: String,
                           splash: Splash,
                           submitter: Submitter,
                           tags: Array[Tags],
                           authors: Array[Author],
                           library: Library
                         ) {
  /**
    * converts this LegacyFormat into the new internal MonaFormat
    *
    * @return
    */
  def asSpectrum: Spectrum = Spectrum(this)
}

/**
  * this is an optional defined library, which declares from which source the spectrum is coming
  *
  * @param id
  * @param description
  * @param link
  * @param tag
  */
case class Library(
                    @(Indexed@field)
                    @(TextIndexed@field)
                    @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                    id: String,

                    @(Indexed@field)
                    @(TextIndexed@field)
                    @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                    library: String,

                    description: String,

                    link: String,

                    @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                    tag: Tags
                  )

/**
  * makes serializations and authorizations simpler
  */
object HelperTypes {

  /**
    * a simple query
    *
    * @param string
    */
  case class WrappedString(string: String)


  /**
    * a login token
    */
  case class LoginResponse(token: String)

  /**
    * a login request
    *
    * @param username
    * @param password
    */
  case class LoginRequest(username: String, password: String)

  /**
    * general information about a token to be retrieved from the server and can be useful for client side applications
    *
    * @param username
    * @param validFrom
    * @param validTo
    * @param roles
    */
  case class LoginInfo(username: String, validFrom: Date, validTo: Date, roles: java.util.List[String])

}


@Document(collection = "SEQUENCE")
case class Sequence(
                     @(Id@field)
                     id: String,
                     value: Long
                   )

@Document(collection = "NEWS")
case class NewsEntry(
                      @(Id@field)
                      id: String,
                      date: Date,
                      title: String,
                      content: String
                    )

@Document(collection = "SPLASH_BLACKLIST")
case class BlacklistedSplash(@(Id@field) splash: String)