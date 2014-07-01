package moa.meta

import moa.Value


class DoubleValue extends Value {

    static constraints = {

    }
    static mapping = {
        version false
    }


    Double doubleValue

    public Double getValue() {
        this.doubleValue
    }

    @Override
    void setValue(Serializable o) {
        this.doubleValue = (Double) o
    }

    @Override
    public String getType(){
        return "double"
    }



}
