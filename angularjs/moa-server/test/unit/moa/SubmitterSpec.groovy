package moa

import grails.test.mixin.TestFor
import org.apache.log4j.Logger
import spock.lang.Unroll

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Submitter)
class SubmitterSpec extends ConstraintUnitSpec {
	Logger logger = Logger.getLogger(this.class)

	def setup() {
		mockForConstraintsTests(Submitter, [new Submitter(firstName: 'my new', lastName: 'submitter', password: 'asd', emailAddress: getEmail(true))])
	}

	@Unroll("test all submitter constraints #field is #error")
	def "test all submitter constraints"() {
		when:
		def obj = new Submitter("$field": val)

		then:
		validateConstraints(obj, field, error)

		where:
		error    | field          | val
		'unique' | 'emailAddress' | getEmail(true)
		// blank and nullable have same error code 'nullable' apparently
		'nullable' | 'emailAddress' | ''
		'nullable' | 'firstName' | ''
		'nullable' | 'lastName' | ''
		'nullable' | 'password' | ''
		//valid test cases
		'valid' | 'spectra' | null
		'valid' | 'firstName' | 'Diego'
		'valid' | 'lastName' | 'Pedrosa'
		'valid' | 'password' | 'asdgf'
		'valid' | 'emailAddress' | 'mymail@yahoo.com'
	}

	void "create a Submitter (object)"() {
		when: 'creating a Submitter'
		Submitter t = new Submitter(firstName: 'my new', lastName: 'submitter', password: 'asd', emailAddress: 'my@new.submitter.com')

		then: 'it should be defined'
		t != null

		and: 'it should have firstName, lastName, passwd, and email'
		t.firstName.equals('my new')
		t.lastName.equals('submitter')
		t.password.equals('asd')
		t.emailAddress.equals('my@new.submitter.com')
	}

	void "save a submitter (persist)"() {
		given:
		def pre_count = Submitter.count
		Submitter t = new Submitter(firstName: 'my new', lastName: 'submitter', password: 'asd', emailAddress: 'my@new.submitter.com')

		when: 'saving a submitter'
		t.save(flush: true)

		then: 'it should exist in the database'
		Submitter.findByEmailAddress('my@new.submitter.com') != null

		and: 'the database should have one more row'
		Submitter.count == pre_count + 1
	}

	void "test Submitter modification"() {
		given:
		Submitter t = new Submitter(firstName: 'sucky', lastName: 'submitter', password: 'asd', emailAddress: 'my@new.submitter.com').save(flush: true)
		def pre_firstName = Submitter.findByEmailAddress('my@new.submitter.com').firstName

		when: 'Submitter gets modified'
		t.firstName = 'better'
		t.save(flush: true)

		then: 'there should be only 1 Submitter with full name better submitter'
		Submitter.count == 1
		Submitter.findByEmailAddress('my@new.submitter.com').firstName.equals('better')
		Submitter.findByEmailAddress('my@new.submitter.com').lastName.equals('submitter')
		pre_firstName.equals('sucky')
	}

	void "test Submitter deletion"() {
		given:
		def pre1_count = Submitter.count
		Submitter t = new Submitter(firstName: 'sucky', lastName: 'submitter', password: 'asd', emailAddress: 'my@new.submitter.com').save(flush: true)
		def pre2_count = Submitter.count

		when: 'Submitter is deleted'
		t.delete()

		then: 'pre1 and pre2 counts should be 0 and 1, and post should be 0'
		pre1_count == 0
		pre2_count == 1
		Submitter.count == 0
	}
}