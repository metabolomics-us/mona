package moa

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 6/30/14
 * Time: 4:44 PM
 */
class Value {

    String type

    Serializable value

    static belongsTo = [metaData: MetaData]

    static mapping = {
    }


    static transients = ['value','type']
}
