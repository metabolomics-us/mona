package curation.rules.spectra

import curation.AbstractCurationRule
import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import groovy.sql.Sql
import moa.Spectrum
import moa.Tag
import moa.server.query.SpectraQueryService
import org.apache.log4j.Logger
import static util.MetaDataFieldNames.*

import javax.sql.DataSource
import static util.MetaDataFieldNames.*

/**
 * remove a spectra incase it's duplicated
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 2/26/15
 * Time: 2:29 PM
 */
class IsDuplicatedSpectraRule extends AbstractCurationRule {

    SpectraQueryService spectraQueryService

    double minSimilarity = 900

    private Logger logger = Logger.getLogger(getClass())



    IsDuplicatedSpectraRule() {

        this.successAction = new RemoveTagAction(DUPLICATED_SPECTRA)
        this.failureAction = new AddTagAction(DUPLICATED_SPECTRA)
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isSpectra()
    }

    @Override
    boolean executeRule(CurationObject toValidate) {

        logger.info("checking for duplicated spectra")
        Spectrum spectrum = toValidate.objectAsSpectra

        def result =  spectraQueryService.findSimilarSpectraIds(spectrum.id, minSimilarity)

        logger.info("duplicated? ${result}")

        return result.isEmpty()

    }


    @Override
    String getDescription() {
        return "this rule calculates if this given spectra is very similar to other spectra. The similarity is estimated based on the dot product approach"
    }
}
