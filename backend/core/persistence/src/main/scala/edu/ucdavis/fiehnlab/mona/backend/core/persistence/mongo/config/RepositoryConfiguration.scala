package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types._
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.ISpectrumRepositoryCustom
import org.springframework.context.annotation.{Import, Bean, Configuration}
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
  * Created by wohlgemuth on 2/25/16.
  */
@Configuration
@EnableMongoRepositories(basePackageClasses = Array(
  classOf[ISpectrumRepositoryCustom]
), excludeFilters = Array())
@Import(Array(classOf[MongoConfig]))
class RepositoryConfiguration {

}
