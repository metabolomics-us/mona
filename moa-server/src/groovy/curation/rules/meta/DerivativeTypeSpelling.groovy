package curation.rules.meta

import curation.actions.MetaDataSuspectAction
import curation.rules.AbstractMetaDataCentricRule
import moa.MetaData
import moa.MetaDataValue
import moa.server.metadata.MetaDataPersistenceService
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/23/15
 * Time: 12:36 PM
 */
class DerivativeTypeSpelling extends AbstractMetaDataCentricRule {

    private Logger logger = Logger.getLogger(getClass())
    private static final String FIELD = "derivative type"

    MetaDataPersistenceService metaDataPersistenceService

    DerivativeTypeSpelling() {
        super(new MetaDataSuspectAction(FIELD, false), new MetaDataSuspectAction(FIELD, true))

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

            newValue = "${matcher[0]} TMS"
        } else if (myValue.matches(regexWrong)) {

            logger.info("value needs to be rewritten!")
            //reformat and update
            def matcher = (myValue =~ regexWrong)

            newValue = "${matcher[0]} TMS"
        } else {

            logger.info("invalid value ($myValue) for ${FIELD}")
            this.getFailureAction().setReason(("provide value, doesn't match any known regular expression"))

            return false
        }

        logger.info("adding new database metadata value")
        //create new metadata object and update it
        metaDataPersistenceService.generateMetaDataObject(value.owner, [name: value.getName(), value: newValue, category: value.getCategory()])

        logger.info("deleting outdated value")
        //delete the wrong object

        value.metaData.removeFromValue(value)
        value.metaData = null
        value.delete()

        return true
    }

    /**
     * checks if we can accept this field
     * @param value
     * @return
     */
    protected boolean isCorrectMetaDataField(MetaDataValue value) {
        logger.debug("checking ${value.name} against defined field ${FIELD}")
        if (value.name.toLowerCase().equals(FIELD.toLowerCase())) {
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
