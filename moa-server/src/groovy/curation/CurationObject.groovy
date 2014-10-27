package curation

import moa.Compound
import moa.MetaDataValue
import moa.Spectrum

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/8/14
 * Time: 2:44 PM
 */
final class CurationObject {

    private Object object

    public CurationObject(Object object){
        this.object = object
    }

    boolean isSpectra(){
        return object instanceof Spectrum
    }

    boolean isMetaData(){
        return object instanceof MetaDataValue
    }

    boolean isCompound(){
        return object instanceof Compound
    }

    void refreshObject(){

        this.object.refresh()
    }

    /**
     * provides us with the spectrum object
     * @return
     */
    Spectrum getObjectAsSpectra(){
        return object as Spectrum
    }

    /**
     * provides us with the compound object
     * @return
     */
    Compound getObjectAsCompound(){
        return object as Compound
    }
    /**
     * provides us with the meta data object
     * @return
     */
    MetaDataValue getObjectAsMetaDataValue(){
        return object as MetaDataValue
    }
}
