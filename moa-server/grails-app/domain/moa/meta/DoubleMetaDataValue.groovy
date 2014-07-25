package moa.meta

import moa.MetaDataValue


class DoubleMetaDataValue extends MetaDataValue {

    static constraints = {

    }
    static mapping = {
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
