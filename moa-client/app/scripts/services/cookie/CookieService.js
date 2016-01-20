/**
 * Created by wohlgemuth on 10/31/14.
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .factory('CookieService', CookieService);

    /* @ngInject */
    function CookieService(ApplicationError, $cookieStore, $log) {

        var service = {
            stringToBoolean: stringToBoolean,
            update: update,
            get: get,
            remove: remove,
            getBooleanValue: getBooleanValue
        };

        return service;

        function stringToBoolean(string) {
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
        function update(name, value) {
            $cookieStore.put(name, value);
        }

        /**
         * gets the cookie
         * @param cookieName
         */
        function get(cookieName) {
            return $cookieStore.get(cookieName);
        }

        /**
         * remove a cookie
         * @param cookieName
         */
        function remove(cookieName) {
            return $cookieStore.remove(cookieName);
        }

        /**
         * provides us with a boolean cookie value of true or false
         * @param cookieName name your cookie
         * @param defaultValueIfNotFound default value if we don't find the cookie
         */
        function getBooleanValue(cookieName, defaultValueIfNotFound) {
            if (defaultValueIfNotFound === null) {
                defaultValueIfNotFound = false;
            }

            var result = defaultValueIfNotFound;

            if ($cookieStore.get(cookieName) !== null) {
                result = stringToBoolean($cookieStore.get(cookieName));
            }

            //return the result
            return result;
        }
    }
})();
