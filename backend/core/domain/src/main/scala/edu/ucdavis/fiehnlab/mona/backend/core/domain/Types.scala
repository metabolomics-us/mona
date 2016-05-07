package edu.ucdavis.fiehnlab.mona.backend.core.domain

import java.util.Date
import javax.validation.constraints.{Size, NotNull}

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import edu.ucdavis.fiehnlab.mona.backend.core.domain.annotation.TupleSerialize
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.NumberDeserializer
import org.hibernate.validator.constraints.Email
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.{Field, FieldIndex, FieldType}
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field
import scala.beans.BeanProperty

/**
  * definition of the MoNA domain classes and accepts arbitrary values, which needs to be supported by different
  * serializers
  */

case class MetaData(
                     @(Field@NotNull)
                     @(Indexed@field)
                     category: String,

                     @(Field@NotNull)
                     @(Indexed@field)
                     computed: Boolean,

                     @(Field@NotNull)
                     @(Indexed@field)
                     hidden: Boolean,

                     @(Field@NotNull)
                     @(Indexed@field)
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
                       *
                       */
                     @(Field@NotNull)
                     @(TupleSerialize@field)
                     @(JsonDeserialize@field)(using = classOf[NumberDeserializer])
                     value: Any

                   )

case class Names(

                  @(Field@NotNull)
                  @(Indexed@field)
                  @(Field@field)(`type` = FieldType.Boolean, index = FieldIndex.not_analyzed)
                  computed: Boolean,

                  @(Field@NotNull)
                  @(Indexed@field)
                  @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                  name: String,

                  score: Double,

                  @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                  source: String
                )


case class Tags(
                 @(Field@NotNull)
                 @(Indexed@field)
                 ruleBased: Boolean,

                 @(Field@NotNull)
                 @(Indexed@field)
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
  */
case class Compound(
                     @(Field@NotNull)
                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     inchi: String,

                     @(Field@NotNull)
                     @(Indexed@field)
                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     inchiKey: String,

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     metaData: Array[MetaData],


                     @(Field@NotNull)
                     molFile: String,

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     names: Array[Names],

                     @(Field@NotNull)

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     tags: Array[Tags],

                     @(Indexed@field)
                     computed: Boolean = false,

                     @(Field@field)(`type` = FieldType.Object)
                     score: Score,

                     @(Field@NotNull)
                     @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                     kind: String = "biological",

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     classification: Array[MetaData] = Array()
                   )

case class Impacts(

                    impactValue: Double,

                    reason: String
                  )


case class Score(

                  impacts: Array[Impacts],
                  @(Field@NotNull)
                  relativeScore: Double, //ns
                  @(Field@NotNull)
                  scaledScore: Double, //ns
                  @(Field@NotNull)
                  score: Double
                )


case class Splash(
                   @(Field@NotNull)
                   @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                   @(Indexed@field)
                   splash: String,
                   @(Field@NotNull)
                   @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                   @(Indexed@field)
                   block1: String,
                   @(Field@NotNull)
                   @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                   @(Indexed@field)
                   block2: String,
                   @(Field@NotNull)
                   @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                   @(Indexed@field)
                   block3: String,
                   @(Field@NotNull)
                   @(Field@field)(`type` = FieldType.String, index = FieldIndex.not_analyzed)
                   @(Indexed@field)
                   block4: String

                 )


/**
  * defines the submitter for a spectrum an is basically the person who uploaded it
  * to the system
  *
  * @param id an internal id
  * @param emailAddress
  * @param firstName
  * @param institution
  * @param lastName
  */
@Document(collection = "SUBMITTER")
case class Submitter(

                      /**
                        * primary id for the user, can be any string
                        */
                      @(Id@field)
                      id: String,
                      @(Field@Email)
                      emailAddress: String,

                      @(Field@NotNull)
                      firstName: String,

                      @(Field@NotNull)
                      institution: String,

                      @(Field@NotNull)
                      lastName: String
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
                   @(Field@Email)
                   @(Indexed@field)
                   emailAddress: String,

                   @(Field@NotNull)
                   @(Indexed@field)
                   firstName: String,

                   @(Field@NotNull)
                   @(Indexed@field)
                   institution: String,

                   @(Field@NotNull)
                   @(Indexed@field)
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
                     @(Field@Size(min=1))
                     @(Field@field)(`type` = FieldType.Nested)
                     compound: Array[Compound],

                     @(Field@NotNull)
                     @(Id@field)
                     @BeanProperty
                     id: String,

                     lastUpdated: String,

                     @(Field@field)(`type` = FieldType.Nested, includeInParent = true)
                     metaData: Array[MetaData],

                     @(Field@field)(`type` = FieldType.Object)
                     score: Score,

                     @(Field@NotNull)
                     @(Field@field)(`type` = FieldType.String)
                     spectrum: String,

                     @(Field@field)(`type` = FieldType.Object)
                     splash: Splash,

                     @(Field@NotNull)
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
      lastUpdated = spectrum.lastUpdated,
      metaData = spectrum.metaData,
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
                           lastUpdated: String,
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
  * this is anm optional defined library, which declares from which source the spectrum is coming
  *
  * @param name
  * @param description
  * @param url
  */
case class Library(
                    name: String,
                    description: String,
                    url: String

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
