package moa

import grails.test.mixin.TestFor
import org.apache.log4j.Logger

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(Spectrum)
class SpectrumSpec extends ConstraintUnitSpec {
	Logger logger = Logger.getLogger(this.class)

	def setup() {
	}

	def cleanup() {
	}

//	void "create a Spectrum (object)"() {
//		when: 'creating a Spectrum'
//		Spectrum t = new Spectrum(spectrum: '100 111, 111 68, 112 25')
//
//		then: 'it should be defined'
//		t != null
//
//		and: 'it should have at least the spectrum data'
//		t.spectrum.equals('100 111, 111 68, 112 25')
//	}
//
//	void "save a Spectrum (persist)"() {
//		given:
//		def pre_count = Spectrum.count
//		Spectrum t = new Spectrum(spectrum: '100 111, 111 68, 112 25')
//
//		when: 'saving a Spectrum'
//		t.save(flush: true)
//
//		then: 'it should exist in the database'
//		Spectrum.findBySpectrum('100 111, 111 68, 112 25') != null
//
//		and: 'the database should have one more row'
//		Spectrum.count == pre_count + 1
//	}
//
//	void "verify email uniqueness"() {
//		given:
//		Spectrum t = new Spectrum(spectrum: '100 111, 111 68, 112 25').save(flush: true)
//		Spectrum tt = new Spectrum(spectrum: '100 111, 111 68, 112 25')
//
//		when: 'saving duplicate Spectrum'
//		tt.save(flush: true)
//
//		then: 'it should fail the unique constraint'
//		!tt.validate()
//		tt.errors['spectrum'].toString().contains("rejected value [100 111, 111 68, 112 25]")
//	}
//
//	void "test Spectrum modification"() {
//		given:
//		Spectrum t = new Spectrum(spectrum: '100 111, 111 68, 112 25').save(flush: true)
//		def pre_spectrum = Spectrum.findBySpectrum('100 111, 111 68, 112 25').spectrum
//
//		when: 'Spectrum gets modified'
//		t.spectrum = '110 111, 111 68, 112 25'
//		t.save(flush: true)
//
//		then: 'there should be only 1 Spectrum with full name better Spectrum'
//		Spectrum.count == 1
//		Spectrum.findBySpectrum('110 111, 111 68, 112 25') != null
//		Spectrum.findBySpectrum('100 111, 111 68, 112 25') == null
//		pre_spectrum.equals('100 111, 111 68, 112 25')
//	}
//
//	void "test Spectrum deletion"() {
//		given:
//		def pre1_count = Spectrum.count
//		Spectrum t = new Spectrum(spectrum: '100 111, 111 68, 112 25').save(flush: true)
//		def pre2_count = Spectrum.count
//
//		when: 'Spectrum is deleted'
//		t.delete()
//
//		then: 'pre1 and pre2 counts should be 0 and 1, and post should be 0'
//		pre1_count == 0
//		pre2_count == 1
//		Spectrum.count == 0
//	}
}
