package util

import moa.MetaDataValue
import moa.meta.BooleanMetaDataValue
import moa.meta.DoubleMetaDataValue
import moa.meta.StringMetaDataValue

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 7/9/14
 * Time: 4:50 PM
 */
class MetaDataValueHelper {

    /**
     * trys to estimate what our object is going to be
     * @param current
     * @return
     */
    static MetaDataValue getValueObject(String current) {
        try {
            Double value = Double.parseDouble(current)
            return (new DoubleMetaDataValue(doubleValue: value))

        } catch (NumberFormatException ex) {
            if (current.toString().toLowerCase().trim() == "true") {
                return (new BooleanMetaDataValue(booleanValue: true))

            } else if (current.toString().toLowerCase().trim() == "false") {
                return (new BooleanMetaDataValue(booleanValue: false))

            } else {
                return (new StringMetaDataValue(stringValue: current))
            }
        }
    }
}
