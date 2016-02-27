package edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo

import com.mongodb.{MongoClient, Mongo}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractMongoConfiguration

/**
  * Created by wohlgemuth on 2/25/16.
  */
@EnableAutoConfiguration
@Configuration
class TestMongoDBConfig extends AbstractMongoConfiguration{
  override def mongo(): Mongo =  new MongoClient("127.0.0.1")

  override def getDatabaseName: String = "mona-test"
}
