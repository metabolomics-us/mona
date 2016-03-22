package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper

import java.io.StringWriter

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.core.{JsonFactory, JsonParser}
import edu.ucdavis.fiehnlab.mona.backend.core.domain.MetaData
import org.scalatest.FunSuite

/**
  * Created by wohlg_000 on 3/10/2016.
  */
class ElasticMedaDataDeserializerTest extends FunSuite {

  def getJson(metaData: MetaData): String = {

    val serializer = new ElasticMetaDataSerializer

    val factory = new JsonFactory()
    val content = new StringWriter()
    val generator = factory.createGenerator(content)
    generator.setPrettyPrinter(new DefaultPrettyPrinter())

    serializer.serialize(metaData, generator, null);
    generator.close()
    content.flush()
    content.toString
  }

  def getParser(string:String) : JsonParser = {
    val factory = new JsonFactory()
    val parser = factory.createParser(string)

    parser
  }

  test("testDeserializeString") {
    val data = MetaData("none", false,  false, "test", null, null, null, "test")

    val json = getJson(data)

    val deserializer = new ElasticMedaDataDeserializer

    val copy = deserializer.deserialize(getParser(json),null)

    assert(copy.value.isInstanceOf[String])
    assert(copy.value == "test")

  }

  test("testDeserializeInt") {
    val data = MetaData("none", false, false, "test", null, null, null, 123)

    val json = getJson(data)

    val deserializer = new ElasticMedaDataDeserializer

    val copy = deserializer.deserialize(getParser(json),null)

    assert(copy.value.isInstanceOf[Int])
    assert(copy.value == 123)

  }

  test("testDeserializeDouble") {
    val data = MetaData("none", false, false, "test", null, null, null, 12.21)

    val json = getJson(data)

    val deserializer = new ElasticMedaDataDeserializer

    val copy = deserializer.deserialize(getParser(json),null)

    assert(copy.value.isInstanceOf[Double])
    assert(copy.value == 12.21)

  }

}
