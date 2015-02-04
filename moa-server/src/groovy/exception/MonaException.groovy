package exception

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 1/28/15
 * Time: 12:00 PM
 */
class MonaException extends RuntimeException {
    MonaException() {
    }

    MonaException(String s) {
        super(s)
    }

    MonaException(String s, Throwable throwable) {
        super(s, throwable)
    }

    MonaException(Throwable throwable) {
        super(throwable)
    }
}
