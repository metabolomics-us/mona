/**
 * Created by wohlgemuth on 10/31/14.
 */

import * as angular from 'angular';

class CookieService{
    private static $inject = ['$cookies', '$log'];
    private $cookies;
    private $log;

    constructor($cookies, $log) {
        this.$cookies = $cookies;
        this.$log = $log;
    }

     stringToBoolean = (string) => {
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
    }

    /**
     * updates the cookie
     * @param name
     * @param value
     */
    update = (name, value) => {
        this.$cookies.put(name, value);
    }

    /**
     * gets the cookie
     * @param cookieName
     */
    get = (cookieName) => {
        return this.$cookies.get(cookieName);
    }

    /**
     * remove a cookie
     * @param cookieName
     */
    remove = (cookieName) => {
        return this.$cookies.remove(cookieName);
    }

    /**
     * provides us with a boolean cookie value of true or false
     * @param cookieName name your cookie
     * @param defaultValueIfNotFound default value if we don't find the cookie
     */
    getBooleanValue = (cookieName, defaultValueIfNotFound) => {
        let result = (defaultValueIfNotFound === null) ? false : defaultValueIfNotFound;

        if (this.$cookies.get(cookieName) !== null) {
            result = this.stringToBoolean(this.$cookies.get(cookieName));
        }

        return result;
    }
}

angular.module('moaClientApp')
    .service('CookieService', CookieService);

