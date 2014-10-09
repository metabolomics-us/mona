package curation.actions

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
    void doAction(Spectrum spectrum) {
        logger.debug("Marking "+ field +" with value "+ mark)

        spectrum.getMetaData().each { MetaDataValue value ->
            if (value.name.toLowerCase().equals(field)) {
                logger.debug("Marked metadata "+ value.name +" for spectrum "+ spectrum.id)

                value.suspect = mark
                value.save(flush: true)
            }
        }
    }
}
