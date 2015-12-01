package curation.actions

import curation.CurationAction
import curation.CurationObject
import moa.MetaDataValue
import moa.Spectrum
import org.apache.log4j.Logger

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 2:09 PM
 */
class MetaDataSuspectAction implements CurationAction {
    private Logger logger = Logger.getLogger(getClass())

    private String field
    private boolean mark
    private String reason = "unknown"

    public MetaDataSuspectAction(String field, boolean mark, String reason) {
        this.field = field
        this.mark = mark
        this.reason = reason
    }

    public MetaDataSuspectAction(String field, boolean mark) {
        this.field = field
        this.mark = mark
        this.reason = ""
    }

    public void setReason(String reason){
        this.reason = reason
    }

    @Override
    boolean actionAppliesToObject(CurationObject toValidate) {
        if (toValidate.objectAsSpectra) {
            return true
        } else if (toValidate.objectAsMetaDataValue) {
            return true
        }

        return false
    }

    @Override
    String getDescription() {
        return "marks the metadata object as suspicious or not"
    }

    @Override
    void doAction(CurationObject curationObject) {
        if (curationObject.isSpectra()) {
            Spectrum spectrum = curationObject.getObjectAsSpectra()

            spectrum.getMetaData().each { MetaDataValue value ->
                checkValue(value)
            }
        } else if (curationObject.isMetaData()) {
            checkValue(curationObject.getObjectAsMetaDataValue())
        }
    }

    /**
     * does the actual chek
     * @param value
     */
    private void checkValue(MetaDataValue value) {
        if(value.name != null) {
            if (value.name.toLowerCase().equals(field.toLowerCase())) {
                logger.debug("Marking metadata " + value.name + " with value " + mark)

                value.suspect = mark

                if (mark) {
                    value.reasonForSuspicion = reason
                    logger.info("reason: ${reason}")
                } else {
                    value.reasonForSuspicion = ""
                }

                value.save(flush: true)

                // TODO: Improve the method of denoting issues with spectra
//                if (value.suspect) {
//
//                    new AddTagAction(SUSPECT_VALUE).doAction(new CurationObject(value.owner))
//                } else {
//                    new RemoveTagAction(SUSPECT_VALUE).doAction(new CurationObject(value.owner))
//                }
            } else {
                logger.debug("ignoring ${value.name}, doesn't match the requested field ${field}")
            }
        }
        else{
            logger.error("skipped ${value.id} since it had no name!")
        }
    }
}
