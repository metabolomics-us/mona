package persistence.metadata.filter

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 11/14/14
 * Time: 10:56 AM
 */
class Filters implements Filter {
    Collection<Filter> filters;

    @Override
    boolean accept(String key, Object value) {
        for (Filter f : filters) {
            if (!f.accept(key, value)) {
                return false;
            }
        }
        return true
    }
}
