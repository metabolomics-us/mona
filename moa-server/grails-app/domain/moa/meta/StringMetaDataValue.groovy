package moa.meta

import moa.MetaDataValue


class StringMetaDataValue extends MetaDataValue {

    static constraints = {
    }

    static mapping = {
        stringValue sqlType: "varchar(5000)"
        version false
        metaData fetch: 'join'
        score  fetch: 'join'
        owner fetch: 'join'

    }

    String stringValue

    public String getValue() {
        this.stringValue
    }

    @Override
    void setValue(Serializable o) {
        this.stringValue = (String) o
    }

    @Override
    public String toString() {
        return "StringMetaDataValue{" +
                "stringValue='" + stringValue + '\'' +
                ", metaData=" + metaData +
                '}';
    }
}
