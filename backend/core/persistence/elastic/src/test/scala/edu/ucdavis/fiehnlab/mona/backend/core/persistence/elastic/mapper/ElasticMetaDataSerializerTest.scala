package edu.ucdavis.fiehnlab.mona.backend.core.persistence.elastic.mapper

import java.io.StringWriter

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.MetaData
import org.scalatest.FunSuite

/**
  * Created by wohlg_000 on 3/10/2016.
  */
class ElasticMetaDataSerializerTest extends FunSuite {

  test("testSerializeString") {
    val serializer = new ElasticMetaDataSerializer
    val factory = new JsonFactory()
    val content = new StringWriter()

    val generator = factory.createGenerator(content)
    generator.setPrettyPrinter(new DefaultPrettyPrinter())

    val data = MetaData("none", computed = false, hidden = false, "test", null, null, null, "test")

    serializer.serialize(data, generator, null)
    generator.close()
    content.flush()
    println(s"content: \n ${content.toString}")

    assert(content.toString.contains("value_text"))
    assert(!content.toString.contains("value_number") )
    assert(!content.toString.contains("value_boolean"))
  }

  test("testSerializeBoolean") {
    val serializer = new ElasticMetaDataSerializer
    val factory = new JsonFactory()
    val content = new StringWriter()

    val generator = factory.createGenerator(content)
    generator.setPrettyPrinter(new DefaultPrettyPrinter())

    val data = MetaData("none", computed = false, hidden = false, "test", null, null, null, true)

    serializer.serialize(data, generator, null)
    generator.close()
    content.flush()
    println(s"content: \n ${content.toString}")

    assert(!content.toString.contains("value_text"))
    assert(!content.toString.contains("value_number") )
    assert(content.toString.contains("value_boolean") )
  }

  test("testSerializeNumber") {
    val serializer = new ElasticMetaDataSerializer
    val factory = new JsonFactory()
    val content = new StringWriter()

    val generator = factory.createGenerator(content)
    generator.setPrettyPrinter(new DefaultPrettyPrinter())

    val data = MetaData("none", computed = false, hidden = false, "test", null, null, null, 123.23)

    serializer.serialize(data, generator, null)
    generator.close()
    content.flush()
    println(s"content: \n ${content.toString}")

    assert(!content.toString.contains("value_text"))
    assert(content.toString.contains("value_number") )
    assert(!content.toString.contains("value_boolean") )
  }
}
