package exception

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 1/29/15
 * Time: 11:18 AM
 */
class ConfigurationError extends MonaException {
    ConfigurationError(String s) {
        super(s)
    }

    ConfigurationError(String s, Throwable throwable) {
        super(s, throwable)
    }

    ConfigurationError(Throwable throwable) {
        super(throwable)
    }

    ConfigurationError() {

    }
}
