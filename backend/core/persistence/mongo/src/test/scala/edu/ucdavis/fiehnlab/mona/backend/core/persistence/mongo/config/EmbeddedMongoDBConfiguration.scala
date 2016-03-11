package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.config

import com.mongodb.{Mongo, MongoClientOptions}
import de.flapdoodle.embed.mongo.config.{IMongodConfig, MongodConfigBuilder}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{MongodExecutable, MongodProcess, MongodStarter}
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.ISpectrumMongoRepositoryCustom
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.context.annotation.{Bean, Configuration, Import}
import org.springframework.core.env.Environment
import org.springframework.data.mongodb.core.{MongoOperations, MongoTemplate}
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

/**
  * Created by wohlg on 3/11/2016.
  */
@EnableMongoRepositories(basePackageClasses = Array(
  classOf[ISpectrumMongoRepositoryCustom]
), excludeFilters = Array())
@Import(Array(classOf[CascadeConfig]))
@Configuration
class EmbeddedMongoDBConfiguration {

  @Autowired(required = false)
  val options: MongoClientOptions = null

  @Autowired
  val enviorment: Environment = null

  @Bean(destroyMethod = "close")
  def mongo(mongodProcess: MongodProcess): Mongo = {
    val net = mongodProcess.getConfig.net()
    val properties = new MongoProperties()
    properties.setHost(net.getServerAddress.getHostName)
    properties.setPort(net.getPort)
    properties.createMongoClient(this.options, enviorment)
  }

  @Bean(destroyMethod = "stop")
  def mongodProcess(mongodExecutable: MongodExecutable): MongodProcess = mongodExecutable.start()

  @Bean(destroyMethod = "stop")
  def mongodExecutable(mongodStarter: MongodStarter, iMongodConfig: IMongodConfig): MongodExecutable = {
    mongodStarter.prepare(iMongodConfig)
  }

  @Bean
  def mongodConfig(): IMongodConfig = {
    new MongodConfigBuilder().version(Version.Main.PRODUCTION)
      .build()
  }

  @Bean
  def mongodStarter(): MongodStarter = MongodStarter.getDefaultInstance

  @Bean(name = Array("mongoOperation","mongoTemplate"))
  def mongoOperations(mongo: Mongo): MongoOperations = {
    new MongoTemplate(mongo, "monatest")
  }
}
