package edu.ucdavis.fiehnlab.mona.backend.core.statistics.mongo.repository.aggregation

trait StatisticsMongoRepository {
  /**
    * Return the aggregation of metadata values for a given field name, sorted by highest number
    * of occurrences. By default, this method inspects fields from the base metadata group; to
    * specify custom values, supply a `metaDataGroup` value.
    *
    * @param name          The target metadata field
    * @param metaDataGroup The metadata group to inspect
    * @return
    */
  def aggregateByName(name: String, metaDataGroup: Option[String] = None): Seq[(AnyVal, Int)]

}
