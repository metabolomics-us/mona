package curation

import curation.scoring.Scoreable
import moa.Compound
import moa.MetaDataValue
import moa.Spectrum
import moa.SupportsMetaData

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

    boolean isSpectra() {
        return object instanceof Spectrum
    }

    boolean isMetaData() {
        return object instanceof MetaDataValue
    }

    boolean isCompound() {
        return object instanceof Compound
    }

    boolean isSupportsMetaDataObject() {
        return  object instanceof SupportsMetaData
    }

    boolean isScoreable() {
        object instanceof Scoreable
    }

    void refreshObject() {

        this.object = this.object.get(this.object.id)
    }

    /**
     * provides us with the spectrum object
     * @return
     */
    Spectrum getObjectAsSpectra() {
        return object as Spectrum
    }

    /**
     * a scorable instance of an object
     * @return
     */
    Scoreable getObjectAsScoreable() {
        return object as Scoreable
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

    SupportsMetaData getObjectAsSupportsMetaData(){
        return object as SupportsMetaData
    }
}
