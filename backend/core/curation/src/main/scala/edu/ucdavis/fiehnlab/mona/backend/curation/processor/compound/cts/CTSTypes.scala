package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.cts

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
                    `type`: String,
                    score: Double
                  )


case class CTSResponse(
                        inchikey: String,
                        inchicode: String,

                        molweight: Double,
                        exactmass: Double,
                        formula: String,

                        synonyms: Array[Synonym],
                        externalIds: Array[ExternalId]
                      )
