package moa.meta

import moa.Value


class IntegerValue extends Value{

    static constraints = {
    }

    static mapping = {
        version false
    }


    Integer integerValue

    public Integer getValue() {
        this.integerValue
    }

    @Override
    void setValue(Serializable o) {
        this.integerValue = (Integer)o
    }

    @Override
    public String getType(){
        return "int"
    }


}
