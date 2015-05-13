package moa.meta

import moa.MetaDataValue


class IntegerMetaDataValue extends MetaDataValue{

    static constraints = {
    }

    static mapping = {
        metaData lazy: false
        score lazy: false
    }


    Integer integerValue

    public Integer getValue() {
        this.integerValue
    }

    @Override
    void setValue(Serializable o) {
        this.integerValue = (Integer)o
    }

}
