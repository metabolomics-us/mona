package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.advice

import com.turkraft.springfilter.exception.{BadFilterSyntaxException, InternalFilterException}
import com.turkraft.springfilter.parser.Filter
import org.scalatest.flatspec.AnyFlatSpec
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping, RestController}

/**
  * Unit tests for the filter exception advice. Driven through a standalone MockMvc setup with a
  * stub controller, so no Spring context, security chain or database is involved
  */
class FilterExceptionHandlerSpec extends AnyFlatSpec {

  /**
    * Stub endpoints raising the exceptions the advice is expected to translate
    *
    * Nested inside the spec on purpose. This package sits under the component scan RestServerConfig
    * declares, and scanning does not separate test classes from main ones, so a top level controller
    * here would be registered as a bean and mount /rest/stub in every integration test context. An
    * inner class is not an independent candidate, so the scanner skips it while MockMvc still takes it
    */
  @RestController
  @RequestMapping(Array("/rest/stub"))
  class FilterThrowingController {

    @GetMapping(path = Array("/bad"))
    def bad(): String = throw new BadFilterSyntaxException("token recognition error at: '='", null)

    @GetMapping(path = Array("/internal"))
    def internal(): String = throw new InternalFilterException("no such path in the entity graph", null)

    @GetMapping(path = Array("/ok"))
    def ok(): String = "fine"
  }

  private val mockMvc: MockMvc = MockMvcBuilders
    .standaloneSetup(new FilterThrowingController)
    .setControllerAdvice(new FilterExceptionHandler)
    .build()

  "FilterExceptionHandler" should "answer 400 for a query the filter parser cannot read" in {
    val response = mockMvc.perform(get("/rest/stub/bad")).andReturn().getResponse

    assert(response.getStatus == 400)
    assert(response.getContentAsString.contains("invalid query syntax"))
    assert(response.getContentAsString.contains("token recognition error"))
  }

  it should "point a rejected query at the supported syntax" in {
    val response = mockMvc.perform(get("/rest/stub/bad")).andReturn().getResponse

    assert(response.getContentAsString.contains("spring-filter syntax"))
  }

  it should "keep an internal filter failure a server error" in {
    val response = mockMvc.perform(get("/rest/stub/internal")).andReturn().getResponse

    assert(response.getStatus == 500)
    assert(response.getContentAsString.contains("could not be evaluated"))
  }

  it should "leave a successful request alone" in {
    val response = mockMvc.perform(get("/rest/stub/ok")).andReturn().getResponse

    assert(response.getStatus == 200)
  }

  /**
    * Guards the assumption the advice is built on. The legacy RSQL syntax MoNA served before the
    * move to spring-filter has no '=' token in the current grammar, which is what produces the
    * rejections this advice reports. Should an upgrade start accepting it, the hint in the response
    * would be wrong and this fails first
    */
  "the filter parser" should "reject the legacy RSQL syntax" in {
    assertThrows[BadFilterSyntaxException] {
      Filter.from("metaData=q='name==\"ionization mode\" and value==\"positive\"'")
    }

    assertThrows[BadFilterSyntaxException] {
      Filter.from("compound.inchiKey==\"KWILGNNWGSNMPA-UHFFFAOYSA-N\"")
    }
  }

  it should "accept the syntax the response hint recommends" in {
    Filter.from("exists(metaData.name:'ionization mode' and metaData.value:'positive')")
  }
}
