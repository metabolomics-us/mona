package moa

import grails.test.mixin.TestFor
import org.apache.log4j.Logger
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Tag)
class TagSpec extends Specification {
	Logger logger = Logger.getLogger(this.class)

	def setup() {

	}

	def cleanup() {
	}

	void "create a tag"() {
		when: 'creating a tag'
		def t = new Tag(text:'my new tag')
		then: 'it should be defined'
		t != null
		then: 'it should have text'
		t.text.equals('my new tag')
	}

	void "verify uniqueness"() {
		given:
		Tag t = new Tag(text:'unique').save(flush:true)
		Tag tt = new Tag(text:'unique')

		when: 'saving duplicate tag'
		tt.save(flush:true)

		then: 'it should fail the unique constraint'
		!tt.validate()
		tt.errors['text'].toString().contains("rejected value [unique]")
	}
}
