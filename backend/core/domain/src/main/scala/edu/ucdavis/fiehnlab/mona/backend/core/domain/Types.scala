package edu.ucdavis.fiehnlab.mona.backend.core.domain

import com.fasterxml.jackson.databind.annotation.{JsonSerialize, JsonDeserialize}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.NumberDeserializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.{DBRef, Document}

import scala.beans.BeanProperty

/**
  * definition of the MoNA domain classes
  * TODO: make DBRefs work with cascade save
  */

object Types {

  case class MetaData(
                       category: String ,
                       computed: Boolean,
                       deleted: Boolean,
                       hidden: Boolean,
                       name: String,
                       score: Score,
                       unit: String,
                       url: String,
                       @JsonDeserialize(using = classOf[NumberDeserializer])
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

  @Document(collection = "COMPOUND")
  case class Compound(
                       id: String,
                       inchi: String,
                       inchiKey: String,
                       metaData: Array[MetaData],
                       molFile: String,
                       names: Array[Names],
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
                     block1: String, //ns
                     block2: String, //ns
                     block3: String, //ns
                     splash: String
                   )


  @Document(collection = "AUTHORITIES")
  case class Authorities(
                          authority: String
                        )

  @Document(collection = "SUBMITTER")
  case class Submitter(
                        accountEnabled: Boolean,
                        accountExpired: Boolean,
                        accountLocked: Boolean,
                        @DBRef
                        authorities: Array[Authorities],
                        emailAddress: String,
                        firstName: String,
                        institution: String,
                        lastName: String,
                        passwordExpired: Boolean //ns
                      )

  @Document(collection = "SPECTRUM")
  case class Spectrum(
                       @DBRef
                       biologicalCompound: Compound,
                       @DBRef
                       chemicalCompound: Compound,
                       @DBRef
                       predictedCompound: Compound,
                       deleted: Boolean,
                       @Id
                       id: String,
                       lastUpdated: String,
                       metaData: Array[MetaData],
                       score: Score,
                       spectrum: String,
                       splash: Splash,
                       @DBRef
                       submitter: Submitter,
                       tags: Array[Tags]
                     )
}

/**
  * makes serializations simpler
  */
object HelperTypes{

  /**
    * a simple query
    * @param query
    */
  case class Query( query:String)

}