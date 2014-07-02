package moa

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 6/30/14
 * Time: 4:44 PM
 */
class MetaDataValue {

    String type

    Serializable value

    static belongsTo = [metaData: MetaData, spectrum:Spectrum]

    static mapping = {
    }

    static constraints = {
        metaData nullable: true
        spectrum nullable: true
    }

    /**
     * associated name of the metadata object
     * @return
     */
    public String getName(){
        return metaData?.name

    }

    /**
     * category for this object
     * @return
     */
    public String getCategory(){
        return metaData?.category?.name
    }

    static transients = ['value','type','name','category']
}
