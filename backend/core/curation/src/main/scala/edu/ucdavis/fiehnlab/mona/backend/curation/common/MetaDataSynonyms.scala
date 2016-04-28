package edu.ucdavis.fiehnlab.mona.backend.curation.common

import java.io.{InputStreamReader, FileInputStream}
import scala.io.Source

/**
  * Created by sajjan on 4/20/16.
  */
object MetaDataSynonyms {
  val ALL_SYNONYMS: Map[String, String] = Map(
    Source.fromURL(getClass.getResource("/mona-metadata-synonyms.tsv"))
      .getLines
      .map(_.stripLineEnd.split('\t'))
      .map(x => x(0) -> x(1)).toList : _*)
}
