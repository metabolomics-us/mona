package moa.meta

import moa.MetaDataValue


class IntegerMetaDataValue extends MetaDataValue{

    static constraints = {
    }

    static mapping = {
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
