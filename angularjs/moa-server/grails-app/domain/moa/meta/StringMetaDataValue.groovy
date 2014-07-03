package moa.meta

import moa.MetaDataValue


class StringMetaDataValue extends MetaDataValue {

    static constraints = {
    }

    static mapping = {
        version false
    }

    String stringValue

    public String getValue() {
        this.stringValue
    }

    @Override
    void setValue(Serializable o) {
        this.stringValue = (String) o
    }

}
