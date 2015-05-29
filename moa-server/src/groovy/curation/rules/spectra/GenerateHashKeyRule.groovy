package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import groovy.sql.Sql
import moa.Spectrum

import javax.sql.DataSource

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 5/28/15
 * Time: 12:05 PM
 */
class GenerateHashKeyRule extends AbstractCurationRule {

    DataSource dataSource

    boolean executeRule(CurationObject toValidate) {

        Spectrum spectrum = toValidate.getObjectAsSpectra()

        Sql sql = Sql.newInstance(dataSource)

        sql.execute("select calculateHash(?)",[spectrum.id])

        return true
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    String getDescription() {
        return "generates an unique hash key for a provided spectra"
    }
}
