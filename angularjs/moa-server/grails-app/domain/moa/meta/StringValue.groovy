package moa.meta

import moa.Value

class StringValue extends Value {

    static constraints = {
    }

    static mapping = {
        version false
    }

    String actualValue

    public String getValue() {
        this.actualValue
    }

    @Override
    void setValue(Serializable o) {
        this.actualValue = (Double) o
    }


}
