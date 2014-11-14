package persistence.metadata.filter

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 11/14/14
 * Time: 10:55 AM
 */
interface Filter {

    /**
     * do we accept this
     * @param name
     * @return
     */
    boolean accept(String key, Object value)
}
