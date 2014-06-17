package moa.persistence

import grails.test.mixin.TestFor
import moa.Tag
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(TagController)
class TagControllerSpec extends Specification {

    def setup() {

        controller.request.contentType = "text/json"

    }

    def cleanup() {
        Tag.findAll().each { it.delete() }
    }

    void "create a tag"() {

        when:

        controller.request.json = new Tag(text: "this is a simple tag")

        controller.save()

        then:
        Tag.list().size() == 1
    }

    void "delete a tag"() {
        given:

        Tag tag = new Tag(text: "1").save(flush: true)

        when:

        params.id= tag.id
        controller.request.json = tag

        System.err.println("request: " + controller.request.getJSON())

        controller.delete()

        then:
        System.err.println("result: " + controller.response.properties)

        Tag.list().size() == 0

    }

    void "update a tag"() {

    }

    void "list all tags"() {

        given:

        new Tag(text: "1").save(flush: true)
        new Tag(text: "2").save(flush: true)

        when:

        controller.index()

        then:


        Tag.list().size() == 2


    }
}