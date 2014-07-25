package moa

import org.apache.log4j.Logger
import spock.lang.Specification

/**
 * Created by diego on 6/23/2014.
 */
class ConstraintUnitSpec extends Specification {
	Logger log = Logger.getLogger(this.class)

	String getEmail(Boolean valid) {
		valid ? "my@email.com" : "not an e@mail"
	}

	void validateConstraints(obj, field, error) {
		def validated = obj.validate()
		if (error && error != 'valid') {
			assert !validated
			assert obj.errors[field]
			assert error == obj.errors[field]
		} else {
			assert !obj.errors[field]
		}
	}
}
