package curation.rules.meta

import curation.actions.MetaDataSuspectAction
import curation.rules.AbstractMetaDataCentricRule
import moa.MetaData
import moa.MetaDataValue
import moa.server.metadata.MetaDataPersistenceService
import org.apache.log4j.Logger
import static util.MetaDataFieldNames.*

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/23/15
 * Time: 12:36 PM
 */
class DerivativeTypeSpelling extends AbstractMetaDataCentricRule {

    private Logger logger = Logger.getLogger(getClass())

    MetaDataPersistenceService metaDataPersistenceService

    DerivativeTypeSpelling() {
        this.successAction = (new MetaDataSuspectAction(DERIVATISATION_TYPE, false))
        this.failureAction = new MetaDataSuspectAction(DERIVATISATION_TYPE, true)

    }

    @Override
    protected boolean acceptMetaDataValue(MetaDataValue value) {

        //n TMS
        def regexCorrect = /([0-9nN]+).[Tt][Mm][Ss]/

        //nTMS
        def regexNearlyCorrect = /([0-9nN]+).*[Tt][Mm][Ss]/

        //TMSn
        def regexWrong = /[Tt][Mm][Ss].*([0-9nN]+)/

        def newValue = ""

        String myValue = value.value.toString()

        logger.info("checking value: ${myValue}")
        if (myValue.matches(regexCorrect)) {
            logger.info("value is perfect!")
            //ok
            return true
        } else if (myValue.matches(regexNearlyCorrect)) {
            logger.info("value needs slight adjustment!")
            //reformat and update
            def matcher = (myValue =~ regexNearlyCorrect)

            newValue = "${matcher[0][1]} TMS"
        } else if (myValue.matches(regexWrong)) {
            logger.info("value needs to be rewritten!")
            //reformat and update
            def matcher = (myValue =~ regexWrong)

            newValue = "${matcher[0][1]} TMS"
        } else {
            logger.info("invalid value ($myValue) for ${DERIVATISATION_TYPE}")
            this.getFailureAction().setReason(("provide value, doesn't match any known regular expression"))

            return false
        }

        //create new metadata object and update it
        logger.info("adding new database metadata value: ${newValue}")
        metaDataPersistenceService.generateMetaDataObject(value.owner, [name: value.getName(), value: newValue, category: value.getCategory()])

        //delete the wrong object
        logger.info("deleting outdated value")
        metaDataPersistenceService.removeMetaDataValue(value)

        return true
    }

    /**
     * checks if we can accept this field
     * @param value
     * @return
     */
    protected boolean isCorrectMetaDataField(MetaDataValue value) {
        logger.debug("checking ${value.name} against defined field ${DERIVATISATION_TYPE}")

        if (value.name.toLowerCase().equals(DERIVATISATION_TYPE.toLowerCase())) {
            return true
        }

        return false
    }

    //if the field doesn't exist, no reason to fail the rule
    protected boolean failByDefault() {
        return false;
    }


    @Override
    String getDescription() {
        return "this rule ensures that we always write the derivative values correct"
    }

}
