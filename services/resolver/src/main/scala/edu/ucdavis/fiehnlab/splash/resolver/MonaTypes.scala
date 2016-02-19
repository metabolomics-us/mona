package edu.ucdavis.fiehnlab.splash.resolver

/**
  * Created by wohlgemuth on 2/18/16.
  */

case class MetaData(
                     category: Option[String],
                     computed: Option[Boolean],
                     dateCreated: Option[String],
                     deleted: Option[Boolean],
                     hidden: Option[Boolean],
                     id: Option[Double],
                     lastUpdated: Option[String],
                     metaDataId: Option[String],
                     name: Option[String],
                     reasonForSuspicion: Option[String],
                     score: Option[String],
                     suspect: Option[Boolean],
                     unit: Option[String],
                     url: Option[String],
                     value: Option[String]
                   )

case class Names(
                  computed: Option[Boolean],
                  lastUpdated: Option[String],
                  name: Option[String],
                  score: Option[String],
                  source: Option[String]
                )

case class Tags(
                 ruleBased: Option[Boolean],
                 text: Option[String]
               )

case class Compound(
                     id: Option[Double],
                     inchi: Option[String],
                     inchiKey: Option[String],
                     lastUpdated: Option[String],
                     metaData: Option[List[MetaData]],
                     molFile: Option[String],
                     names: Option[List[Names]],
                     tags: Option[List[Tags]]
                   )


case class Impacts(
                    impactValue: Option[Double],
                    reason: Option[String]
                  )

case class Score(
                  impacts: Option[List[Impacts]],
                  relativeScore: Option[Double],
                  scaledScore: Option[Double],
                  score: Option[Double]
                )

case class Splash(
                   block1: Option[String],
                   block2: Option[String],
                   block3: Option[String],
                   id: Option[Double],
                   splash: Option[String]
                 )

case class Authorities(
                        authority: Option[String]
                      )

case class Submitter(
                      accountEnabled: Option[Boolean],
                      accountExpired: Option[Boolean],
                      accountLocked: Option[Boolean],
                      authorities: Option[List[Authorities]],
                      emailAddress: Option[String],
                      firstName: Option[String],
                      id: Option[Double],
                      institution: Option[String],
                      lastName: Option[String],
                      passwordExpired: Option[Boolean]
                    )

case class Spectrum(
                     biologicalCompound: Option[Compound],
                     chemicalCompound: Option[Compound],
                     deleted: Option[Boolean],
                     hash: Option[String],
                     id: Option[Double],
                     lastUpdated: Option[String],
                     metaData: Option[List[MetaData]],
                     predictedCompound: Option[String],
                     score: Option[Score],
                     spectrum: Option[String],
                     splash: Option[Splash],
                     submitter: Option[Submitter],
                     tags: Option[List[Tags]]
                   )