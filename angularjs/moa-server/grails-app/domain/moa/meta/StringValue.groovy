package moa.meta

import moa.Value


class StringValue extends Value {

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

    @Override
    public String getType(){
        return "string"
    }


}
