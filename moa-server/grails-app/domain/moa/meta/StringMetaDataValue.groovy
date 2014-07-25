package moa.meta

import moa.MetaDataValue


class StringMetaDataValue extends MetaDataValue {

    static constraints = {
    }

    static mapping = {
        stringValue sqlType: "varchar(5000)"

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
