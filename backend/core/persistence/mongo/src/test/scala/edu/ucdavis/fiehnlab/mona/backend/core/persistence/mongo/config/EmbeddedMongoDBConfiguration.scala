package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import java.io.File

import com.mongodb.{Mongo, MongoClientOptions}
import com.typesafe.scalalogging.LazyLogging
import de.flapdoodle.embed.mongo.config._
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{Command, MongodExecutable, MongodProcess, MongodStarter}
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.extract.UserTempNaming
import de.flapdoodle.embed.process.io.{IStreamProcessor, Processors}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.config.DomainConfig
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.ISpectrumMongoRepositoryCustom
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration
import org.springframework.boot.autoconfigure.mongo.{MongoAutoConfiguration, MongoProperties}
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.core.{MongoOperations, MongoTemplate}
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
  * Created by wohlg on 3/11/2016.
  */
@EnableMongoRepositories(basePackageClasses = Array(
  classOf[ISpectrumMongoRepositoryCustom]
))
@Import(Array(classOf[CascadeConfig],classOf[DomainConfig],classOf[EmbeddedMongoAutoConfiguration]))
@EnableAutoConfiguration
@Configuration
class EmbeddedMongoDBConfiguration extends LazyLogging{

}
