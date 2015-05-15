package moa

import curation.scoring.Scoreable
import moa.scoring.Score

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 6/30/14
 * Time: 4:44 PM
 */
class MetaDataValue implements Scoreable {

    Date dateCreated
    Date lastUpdated

    Serializable value

    /**
     * unit it's stored in
     */
    String unit

    /**
     * something might be wrong with this value
     */
    boolean suspect = false

    /**
     * reason being for value being suspicious
     */
    @Deprecated
    String reasonForSuspicion = ""

    /**
     * was this value computed or user provided
     */
    boolean computed = false

    static belongsTo = [metaData: MetaData, owner: SupportsMetaData]

    /**
     * score of this object
     */
    Score score

    static mapping = {
        version false
 //       metaData fetch: 'join'
 //       score fetch: 'join'
    }


    static constraints = {
        metaData nullable: true
        owner nullable: true
        unit nullable: true
        suspect nullable: true
        computed nullable: true
        reasonForSuspicion nullable: true
        score nullable: true
    }

    /**
     * associated name of the metadata object
     * @return
     */
    public String getName() {
        return metaData?.name

    }

    /**
     * which type we are
     * @return
     */
    public String getType() {
        return metaData?.type
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof MetaDataValue)) return false

        MetaDataValue that = (MetaDataValue) o

        if (metaData != that.metaData) return false
        if (unit != that.unit) return false
        if (value != that.value) return false

        return true
    }


    public String getCategory() {
        return metaData?.category?.name
    }

    static transients = ['value', 'type', 'name', 'category']

    @Override
    public String toString() {
        return "MetaDataValue{" +
                "value=" + value +
                ", metaData=" + metaData +
                '}';
    }
}
