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
class EmbeddedMongoDBConfiguration extends LazyLogging{

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
  def mongodProcess(mongodExecutable: MongodExecutable): MongodProcess = {
    logger.warn("creating new mongodb process")
    mongodExecutable.start()
  }

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
  def mongodStarter(): MongodStarter = {
    val outPut = Processors.named("[mongo >",new IStreamProcessor {

      override def onProcessed(): Unit = {}
      override def process(block: String): Unit = logger.debug(block)
    })
    val config = new RuntimeConfigBuilder().defaults(Command.MongoD).artifactStore(new ExtractedArtifactStoreBuilder().defaults(Command.MongoD).download(new DownloadConfigBuilder().defaultsForCommand(Command.MongoD).build()).executableNaming(new UserTempNaming)).processOutput(new ProcessOutput(outPut,outPut,outPut)).build()
    MongodStarter.getInstance(config)
  }

  @Bean(name = Array("mongoOperation","mongoTemplate"))
  def mongoOperations(mongo: Mongo): MongoOperations = {
    logger.warn("creating new mongodb template")

    new MongoTemplate(mongo, "monatest")
  }
}
