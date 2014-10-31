/**
 * Created by wohlgemuth on 10/31/14.
 */
app.service('CookieService', function (ApplicationError, $cookieStore, $log) {

    this.stringToBoolean = function (string) {
        switch (string) {
            case "true":
            case "yes":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
            case null:
                return false;
            default:
                return Boolean(string);
        }
    };

    /**
     * updates the cookie
     * @param name
     */
    this.update = function (name, value) {
        $cookieStore.put(name, value);
    };

    /**
     * provides us with a boolean cookie value of true or false
     * @param cookieName name your cookie
     * @param defaultValueIfNotFound default value if we don't find the cookie
     */
    this.getBooleanValue = function (cookieName, defaultValueIfNotFound) {

        if (defaultValueIfNotFound == null) {
            defaultValueIfNotFound = false;
        }

        var result = defaultValueIfNotFound;

        if ($cookieStore.get(cookieName) != null) {
            result = this.stringToBoolean($cookieStore.get(cookieName));
        }

        //return the result
        return result;

    }

});