package moa

class SupportsMetaData {

    static constraints = {
    }

    static mapping = {
        tablePerSubclass true
        version false
        metaData cascade: 'all-delete-orphan', batchSize: 50//,lazy:false
        //links  lazy:false
    }

    Date dateCreated
    Date lastUpdated
    static hasMany = [metaData: MetaDataValue, links: TagLink]

    /**
     * little helper method
     * @return
     */
    Collection<Tag> getTags() {
        def tags = []

        links.each {
            tags.add(it.tag)
        }

        Collections.unmodifiableCollection(tags)
    }

    /**
     * provides us access with the avaialbe values
     * @return
     */
    Collection<MetaDataValue> listAvailableValues(){
        Set data = new HashSet()

        metaData.each {
            if(it.deleted == false){
                data.add(it)
            }
        }

        return data
    }
}
