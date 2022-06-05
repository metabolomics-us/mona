package edu.ucdavis.fiehnlab.mona.backend.core.domain

import java.util.Date
import javax.validation.constraints._

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import edu.ucdavis.fiehnlab.mona.backend.core.domain.annotation.{AnalyzedStringSerialize, TupleSerialize}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.NumberDeserializer
import org.hibernate.validator.constraints.NotEmpty
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.{Field, FieldType}
import org.springframework.data.mongodb.core.index.{Indexed, TextIndexed}
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field
import scala.beans.BeanProperty
import scala.collection.mutable.ArrayBuffer

/**
  * definition of the MoNA domain classes and accepts arbitrary values, which needs to be supported by different
  * serializers
  */

case class MetaData(
                     category: String,
                     computed: Boolean,
                     hidden: Boolean,
                     name: String,
                     score: Score,
                     unit: String,
                     url: String,
                     @(JsonDeserialize@field)(using = classOf[NumberDeserializer])
                     value: Any
                   )

case class Names(
                  computed: Boolean,
                  name: String,
                  score: Double,
                  source: String
                )


case class Tags(
                 ruleBased: Boolean,
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
                     inchi: String,

                     @Deprecated
                     inchiKey: String,
                     metaData: Array[MetaData],
                     molFile: String,
                     names: Array[Names],
                     tags: Array[Tags],
                     computed: Boolean = false,
                     score: Score,
                     kind: String = "biological",
                     classification: Array[MetaData] = Array()
                   )

case class Impact(value: Double, reason: String)

case class Score(
                  impacts: Array[Impact],
                  score: Double
                )

case class Splash(
                   block1: String, //ns
                   block2: String, //ns
                   block3: String, //ns
                   block4: String, //ns
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
                      id: String,
                      emailAddress: String,
                      firstName: String,
                      lastName: String,
                      institution: String
                    ) {

  override def toString: String = {
    val s: ArrayBuffer[String] = new ArrayBuffer

    if (this.firstName != null && this.firstName.nonEmpty) {
      s.append(this.firstName)
    }

    if (this.lastName != null && this.lastName.nonEmpty) {
      s.append(this.lastName)
    }

    if (this.institution != null && this.institution.nonEmpty) {
      s.append(s"(${this.institution})")
    }

    s.mkString(" ")
  }
}


/**
  * this defines an author, which actually acquired the spectra
  *
  * @param emailAddress
  * @param firstName
  * @param institution
  * @param lastName
  */
case class Author(
                   emailAddress: String,
                   firstName: String,
                   institution: String,
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
case class Spectrum(
                     compound: Array[Compound],
                     id: String,

                     dateCreated: Date,
                     lastUpdated: Date,
                     lastCurated: Date = null,
                     metaData: Array[MetaData],
                     annotations: Array[MetaData],
                     score: Score,
                     spectrum: String,
                     splash: Splash,
                     submitter: Submitter,
                     tags: Array[Tags],
                     authors: Array[Author],
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
      lastCurated = null,
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
                    id: String,
                    library: String,
                    description: String,
                    link: String,
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
                     id: String,
                     value: Long
                   )

@Document(collection = "NEWS")
case class NewsEntry(
                      id: String,
                      date: Date,
                      title: String,
                      content: String
                    )

@Document(collection = "SPLASH_BLACKLIST")
case class BlacklistedSplash(@(Id@field) splash: String)

@Document(collection = "SPECTRUM_FEEDBACK")
case class SpectrumFeedback(
                            id: String,
                            monaID: String,
                            userID: String,
                            name: String,
                            value: String
                          )
