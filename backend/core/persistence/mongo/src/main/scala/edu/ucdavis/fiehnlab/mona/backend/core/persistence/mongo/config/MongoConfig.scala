package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import java.util

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Tags
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.{ISpectrumMongoRepositoryCustom, ISubmitterMongoRepository}
import org.springframework.context.annotation.{Bean, ComponentScan, Configuration, Import}
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.{MongoOperations, MongoTemplate}
import org.springframework.data.mongodb.core.convert.{CustomConversions, DefaultDbRefResolver, DefaultMongoTypeMapper, MappingMongoConverter}
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
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
class MongoConfig {

  @Bean
  def mongoOperations(mongoDbFactory: MongoDbFactory, context: MongoMappingContext): MongoOperations = {
    val converter: MappingMongoConverter = new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory), context)
    converter.setTypeMapper(new DefaultMongoTypeMapper(null))
    converter.afterPropertiesSet()

    new MongoTemplate(mongoDbFactory, converter)
  }
}