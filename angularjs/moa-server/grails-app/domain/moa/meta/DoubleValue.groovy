package moa.meta

import moa.Value

class DoubleValue extends Value {

    static constraints = {

    }
    static mapping = {
        version false
    }


    Double actualValue

    public Double getValue() {
        this.actualValue
    }

    @Override
    void setValue(Serializable o) {
        this.actualValue = (Double) o
    }


}
