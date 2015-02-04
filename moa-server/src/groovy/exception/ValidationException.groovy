package exception

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 1/28/15
 * Time: 12:01 PM
 */
class ValidationException extends MonaException{
    ValidationException() {
    }

    ValidationException(String s) {
        super(s)
    }

    ValidationException(String s, Throwable throwable) {
        super(s, throwable)
    }

    ValidationException(Throwable throwable) {
        super(throwable)
    }
}
