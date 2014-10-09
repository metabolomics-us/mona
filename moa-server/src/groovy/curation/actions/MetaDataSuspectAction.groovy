package curation.actions

import curation.CurationObject
import moa.MetaDataValue
import moa.Spectrum
import curation.CurationAction
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

    public MetaDataSuspectAction(String field, boolean mark) {
        this.field = field
        this.mark = mark
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
    void doAction(CurationObject curationObject) {
        if (currationObject.isSpectra()) {
            Spectrum spectrum = currationObject.getObjectAsSpectra()

            spectrum.getMetaData().each { MetaDataValue value ->


                checkValue(value)
            }

            //add a label if the valus are odd
            if (mark) {
                new AddTagAction("suspect values").doAction(currationObject)
            }
            //remove the label if the values are ok
            else {
                new RemoveTagAction("suspect values").doAction(currationObject)
            }
        } else if (currationObject.isMetaData()) {
            checkValue(currationObject.getObjectAsMetaDataValue())
        }


    }

    /**
     * does the actual chek
     * @param value
     */
    private void checkValue(MetaDataValue value) {
        if (value.name.toLowerCase().equals(field)) {
            value.suspect = mark
            value.save(flush: true)
        }
    }
}
