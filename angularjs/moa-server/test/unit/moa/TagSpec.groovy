package moa

import grails.test.mixin.TestFor
import org.apache.log4j.Logger
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Tag)
class TagSpec extends ConstraintUnitSpec {
	Logger logger = Logger.getLogger(this.class)

	def setup() {
		mockForConstraintsTests(Tag, [new Tag(text: 'my dupped tag')])
	}

	def cleanup() {
		Tag.deleteAll(Tag.list())
	}

	@Unroll("test all tag constraints #field is #error")
	def "test all tag constraints"() {
		when:
		def obj = new Tag("$field": val)

		then:
		validateConstraints(obj, field, error)

		where:
		error      | field  | val
		'unique'   | 'text' | 'my dupped tag'
		'nullable' | 'text' | null
		// blank and nullable have same error code 'nullable' apparently
		'nullable' | 'text' | ''
		//valid test cases
		'valid' | 'text' | 'Dirty'
	}

	void "create a tag (object)"() {
		when: 'creating a tag'
		def t = new Tag(text: 'my new tag')

		then: 'it should be defined'
		t != null

		and: 'it should have text'
		t.text.equals('my new tag')
	}

	void "save a tag (persist)"() {
		given:
		def pre_count = Tag.count
		Tag t = new Tag(text: 'my new tag')

		when: 'saving a tag'
		t.save(flush: true)

		then: 'it should exist in the database'
		Tag.findByText('my new tag') != null

		and: 'the database should have one more row'
		Tag.count == pre_count + 1
	}

	void "test tag modification"() {
		given:
		Tag t = Tag.findOrCreateWhere(text: 'tag sucks').save()
		def pre_text = Tag.findByText('tag sucks').text

		when: 'tag gets modified'
		t.text = 'better tag'
		t.save(flush: true)

		then: 'there should be only 1 tag with text \'better tag\''
		Tag.count == 1
		Tag.findByText('better tag') != null
		pre_text.equals('tag sucks')
		Tag.findByText(pre_text) == null
	}

	void "test tag deletion"() {
		given:
		def pre1_count = Tag.count
		Tag t = Tag.findOrCreateWhere(text: 'bye tag').save(flush: true)
		def pre2_count = Tag.count

		when: 'tag is deleted'
		t.delete()

		then: 'pre1 and pre2 counts should be 0 and 1, and post should be 0'
		pre1_count == 0
		pre2_count == 1
		Tag.count == 0
	}
}
