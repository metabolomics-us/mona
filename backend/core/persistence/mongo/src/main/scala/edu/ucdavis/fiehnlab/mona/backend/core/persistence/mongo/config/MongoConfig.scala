package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.{ISpectrumMongoRepositoryCustom, ISubmitterMongoRepository}
import org.springframework.context.annotation.{ComponentScan, Configuration, Import}
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
  * Mongo specific database configuration
  */
@Configuration
@Import(Array(classOf[DomainConfig]))
@EnableMongoRepositories(basePackageClasses = Array(
  classOf[ISpectrumMongoRepositoryCustom]
))
@ComponentScan(basePackageClasses = Array(classOf[ISubmitterMongoRepository]))
class MongoConfig extends LazyLogging