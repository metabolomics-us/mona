package moa

class News implements Comparable {

    static String ANNOUNCEMENT = "announcement"
    static String UPLOAD = "upload"
    static String NOTIFICATION = "notification"
    static String MILESTONE = "milestone"


    static constraints = {

        description nullable : true
        title nullable : false
    }

    static mapping = {
        sort id:"desc"
        version false
        iconClass defaultValue : "'none'"
        expires defaultValue :  0
        title  sqlType: "text"
        description  sqlType: "text"
    }

    /**
     * what kind of icon shall it have
     */
    String iconClass

    /**
     * title of this item
     */
    String title

    /**
     * description of this item
     */
    String description

    /**
     * what kind of item is it
     */
    String type

    Date dateCreated

    Date lastUpdated

    /**
     * where is this item linked too
     */
    String url
    /**
     *
     * how long will this item exist in the database in seconds
     */
    long expires

    @Override
    int compareTo(Object o) {
        if(this.id != null) {
            if(o instanceof News) {
                return this.id.compareTo(o.id)
            }
        }
        return -1
    }
}
