/**
 * Created by wohlgemuth on 10/31/14.
 */

(function() {
    'use strict';

    cookieService.$inject = ['$cookieStore', '$log'];
    angular.module('moaClientApp')
        .factory('CookieService', cookieService);

    /* @ngInject */
    function cookieService($cookieStore, $log) {

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
            var result = (defaultValueIfNotFound === null) ? false : defaultValueIfNotFound;

            if ($cookieStore.get(cookieName) !== null) {
                result = stringToBoolean($cookieStore.get(cookieName));
            }

            return result;
        }

        return {
            stringToBoolean: stringToBoolean,
            update: update,
            get: get,
            remove: remove,
            getBooleanValue: getBooleanValue
        };
    }
})();
