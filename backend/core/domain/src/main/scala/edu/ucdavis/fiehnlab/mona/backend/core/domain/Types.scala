package edu.ucdavis.fiehnlab.mona.backend.core.domain

/**
  * definition of the MoNA domain classes
  */

case class MetaData(
                     category: Option[String],
                     computed: Option[Boolean],
                     deleted: Option[Boolean],
                     hidden: Option[Boolean],
                     name: Option[String],
                     score: Option[List[Score]],
                     unit: Option[String],
                     url: Option[String],
                     value: Option[Any]
                   )

case class Names(
                  computed: Option[Boolean],
                  name: Option[String],
                  score: Option[Double],
                  source: Option[String]
                )

case class Tags(
                 ruleBased: Option[Boolean],
                 text: Option[String]
               )

case class Compound(
                     id: Option[String],
                     inchi: Option[String],
                     inchiKey: Option[String],
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
                  relativeScore: Option[Double], //ns
                  scaledScore: Option[Double], //ns
                  score: Option[Double]
                )

case class Splash(
                   block1: Option[String], //ns
                   block2: Option[String], //ns
                   block3: Option[String], //ns
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
                      institution: Option[String],
                      lastName: Option[String],
                      passwordExpired: Option[Boolean] //ns
                    )

case class Spectrum(
                     biologicalCompound: Option[Compound],
                     chemicalCompound: Option[Compound],
                     predictedCompound: Option[Compound],
                     deleted: Option[Boolean],
                     id: Option[String],
                     lastUpdated: Option[String],
                     metaData: Option[List[MetaData]],
                     score: Option[Score],
                     spectrum: Option[String],
                     splash: Option[Splash],
                     submitter: Option[Submitter],
                     tags: Option[List[Tags]]
                   )
