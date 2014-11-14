package persistence.metadata.filter

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 11/14/14
 * Time: 11:00 AM
 */
class NameDoesntMatchFilter implements Filter {

    String pattern

    NameDoesntMatchFilter(){

    }

    NameDoesntMatchFilter(String pattern){
        this.pattern = pattern
    }

    @Override
    boolean accept(String key, Object value) {
        return !key.matches(pattern)
    }
}
