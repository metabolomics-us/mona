package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import scala.collection.JavaConverters._

/**
  * Created by wohlgemuth on 2/26/16.
  */
@Repository
class ISpectrumRepositoryCustomImpl extends SpectrumRepositoryCustom {

  @Autowired
  val mongoOperations: MongoOperations = null

  /**
    * provide a query to receive a list of the provided  objects
    *
    * @param query
    * @return
    */
  override def executeQuery(query: Query): List[Spectrum] = {
    mongoOperations.find(query, classOf[Spectrum]).asScala.toList
  }
}
