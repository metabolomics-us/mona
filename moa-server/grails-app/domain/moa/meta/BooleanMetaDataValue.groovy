package moa.meta

import moa.MetaDataValue

class BooleanMetaDataValue extends MetaDataValue{

    static constraints = {
    }

    static mapping = {
    }

    String booleanValue

    public Boolean getValue() {
        this.booleanValue
    }

    @Override
    void setValue(Serializable o) {
        this.booleanValue = (Boolean) o
    }

}
