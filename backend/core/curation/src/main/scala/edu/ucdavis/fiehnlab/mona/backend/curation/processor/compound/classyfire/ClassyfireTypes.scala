package edu.ucdavis.fiehnlab.mona.backend.curation.processor.compound.classyfire

/**
  * Created by sajjan on 1/31/17.
  */
case class Classification(
                           name: String,
                           description: String,
                           chemont_id: String,
                           url: String
                         )

case class Classifier(
                       smiles: String,
                       inchikey: String,
                       kingdom: Classification,
                       superclass: Classification,
                       `class`: Classification,
                       subclass: Classification,
                       intermediate_nodes: List[Classification],
                       direct_parent: Classification,
                       alternative_parents: List[Classification],
                       molecular_framework: String,
                       substituents: List[String],
                       description: String,
                       external_descriptors: List[External_descriptors],
                       predicted_chebi_terms: List[String],
                       predicted_lipidmaps_terms: List[String],
                       classification_version: String
                     )

case class External_descriptors(
                                 source: String,
                                 source_id: String,
                                 annotations: List[String]
                               )
