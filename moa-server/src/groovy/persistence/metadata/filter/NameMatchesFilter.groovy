package persistence.metadata.filter

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 11/14/14
 * Time: 11:00 AM
 */
class NameMatchesFilter implements Filter {

    String pattern

    NameMatchesFilter() {

    }

    NameMatchesFilter(String pattern) {
        this.pattern = pattern
    }

    @Override
    boolean accept(String key, Object value) {
        return key.matches(pattern)
    }
}
