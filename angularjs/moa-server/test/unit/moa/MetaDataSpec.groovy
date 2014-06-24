package moa

import grails.test.mixin.TestFor
import org.apache.log4j.Logger

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(MetaData)
class MetaDataSpec extends ConstraintUnitSpec {
	Logger logger = Logger.getLogger(this.class)

	def setup() {
	}

	def cleanup() {
	}

	void "create a MetaData (object)"() {
		when: 'creating a MetaData'
		MetaData t = new MetaData(key: 'instrument', value: 'q-crap', type: 'java.lang.String')

		then: 'it should be defined'
		t != null

		and: 'it should have key, value, and type'
		t.key.equals('instrument')
		t.value.equals('q-crap')
		t.type.equals('java.lang.String')
	}

	void "save a MetaData (persist)"() {
		given:
		def pre_count = MetaData.count
		MetaData t = new MetaData(key: 'instrument', value: 'q-crap', type: 'java.lang.String')

		when: 'saving a MetaData'
		t.save(flush: true)

		then: 'the database should have one more row'
		MetaData.count == pre_count + 1

		and: 'it should exist in the database'
		MetaData.findByKeyAndValue('instrument', 'q-crap') != null
	}

	void "test MetaData modification"() {
		given:
		MetaData t = new MetaData(key: 'instrument', value: 'q-crap', type: 'java.lang.String').save()
		def pre_firstName = MetaData.findByKeyAndValue('instrument', 'q-crap').value

		when: 'MetaData gets modified'
		t.value = 'q-tof'
		t.save(flush: true)

		then: 'there should be only 1 MetaData with value \'q-tof\''
		MetaData.count == 1
		MetaData.findByKeyAndValue('instrument', 'q-tof') != null
		pre_firstName.equals('q-crap')
		MetaData.findByValue(pre_firstName) == null
	}

	void "test MetaData deletion"() {
		given:
		def pre1_count = MetaData.count
		MetaData t = new MetaData(key: 'instrument', value: 'q-crap', type: 'java.lang.String').save(flush: true)
		def pre2_count = MetaData.count

		when: 'MetaData is deleted'
		t.delete()

		then: 'pre1 and pre2 counts should be 0 and 1, and post should be 0'
		pre1_count == 0
		pre2_count == 1
		MetaData.count == 0
	}
}
