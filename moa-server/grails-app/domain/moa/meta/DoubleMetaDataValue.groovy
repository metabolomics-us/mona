package moa.meta

import moa.MetaDataValue


class DoubleMetaDataValue extends MetaDataValue {

    static constraints = {

    }
    static mapping = {
        metaData lazy: false
        score lazy: false
    }


    Double doubleValue

    public Double getValue() {
        this.doubleValue
    }

    @Override
    void setValue(Serializable o) {
        this.doubleValue = (Double) o
    }
}
