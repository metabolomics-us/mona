package edu.ucdavis.fiehnlab.mona.backend.core.domain

import com.fasterxml.jackson.databind.annotation.{JsonSerialize, JsonDeserialize}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.domain.annotation.CascadeSave
import edu.ucdavis.fiehnlab.mona.backend.core.domain.io.json.NumberDeserializer
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.{DBRef, Document}

import scala.annotation.meta.field
import scala.beans.BeanProperty

/**
  * definition of the MoNA domain classes
  * TODO: make DBRefs work with cascade save
  */

object Types {

  case class MetaData(
                       category: String,
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

  case class Submitter(
                        emailAddress: String,
                        firstName: String,
                        institution: String,
                        lastName: String
                      )

  case class Author(
                     emailAddress: String,
                     firstName: String,
                     institution: String,
                     lastName: String
                    )

  @Document(collection = "SPECTRUM")
  case class Spectrum(
                       biologicalCompound: Compound,
                       chemicalCompound: Compound,
                       predictedCompound: Compound,
                       deleted: Boolean,
                       @(Id@field)
                       id: String,
                       lastUpdated: String,
                       metaData: Array[MetaData],
                       score: Score,
                       spectrum: String,
                       splash: Splash,
                       submitter: Submitter,
                       tags: Array[Tags],
                       authors:Array[Author]
                     )

}

/**
  * makes serializations simpler
  */
object HelperTypes {

  /**
    * a simple query
    *
    * @param query
    */
  case class Query(query: String)

}