package moa.meta

import moa.Value

class IntegerValue extends Value{

    static constraints = {
    }

    static mapping = {
        version false
    }


    Integer actualValue

    public Integer getValue() {
        this.actualValue
    }

    @Override
    void setValue(Serializable o) {
        this.actualValue = (Double)o
    }


}
