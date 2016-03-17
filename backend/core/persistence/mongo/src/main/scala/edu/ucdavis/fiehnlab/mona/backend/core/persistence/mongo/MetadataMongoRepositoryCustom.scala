package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rsql.RSQLRepositoryCustom
import org.springframework.data.mongodb.core.query.Query

trait MetadataMongoRepositoryCustom extends RSQLRepositoryCustom[Spectrum,Query] {
  /**
    * Return the aggregated list of unique values for a given metadata field. The results are sorted
    * in descending order. By default, this method returns the top 10 results from the base metadata
    * group; to specify custom values, supply `numResults` and `metaDataGroup`, respectively.
    *
    * @param name The target metadata field
    * @param skip The number of results to drop
    * @param limit The number of results to take
    * @param metaDataGroup The metadata group to inspect
    * @return
    */
  def listTopAggregates(name: String, skip: Int = 0, limit: Long = 10, metaDataGroup: Option[String] = None): Seq[(AnyVal, Int)]
}
