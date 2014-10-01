package curation.actions

import moa.MetaDataValue
import moa.Spectrum
import curation.CurationAction

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/30/14
 * Time: 2:09 PM
 */
class MetaDataSuspectAction implements CurationAction{

    private String field
    private boolean mark
    public MetaDataSuspectAction(String field, boolean mark){
        this.field = field
        this.mark = mark
    }
    @Override
    void doAction(Spectrum spectrum) {
        spectrum.getMetaData().each {MetaDataValue value ->

            if(value.name.toLowerCase().equals(field)){
                value.suspect = mark
                value.save(flush:true)
            }
        }
    }
}
