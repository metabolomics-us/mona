package edu.ucdavis.fiehnlab.mona.backend.curation

/**
  * Created by sajjan on 3/21/16.
  */
case class ExternalId(
                       name: String,
                       value: String,
                       url: String
                     )

case class Synonym(
                    name: String,
                    nameType: String,
                    score: Double
                  )


case class CTSResponse(
                        inchiKey: String,
                        inchicode: String,

                        formula: String,
                        molweight: Double,
                        exactmass: Double,

                        synonyms: Array[Synonym],
                        externalIds: Array[ExternalId]
                      )