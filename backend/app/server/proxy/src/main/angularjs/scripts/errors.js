/**
 * Created by wohlgemuth on 6/12/14.
 *
 * This file contains all the hookups for error handling in the application
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
        /* @ngInject */
        .run(['$rootScope', function($rootScope) {
            //contains all our errors
            $rootScope.errors = [];

            /**
             * adds an error
             * @param error
             */
            $rootScope.addError = function(error) {
                $rootScope.errors.push(error);
            };

            /**
             * clears all errors
             */
            $rootScope.clearErrors = function() {
                $rootScope.errors = [];
            }
        }])


        /**
         * general error handling
         */
        /* @ngInject */
        .config(['$provide', function($provide) {
            $provide.decorator("$exceptionHandler", ['$delegate', '$injector', function($delegate, $injector) {
                return function(exception, cause) {
                    var $rootScope = $injector.get("$rootScope");
                    $rootScope.addError({message: "Exception", reason: exception});
                    $delegate(exception, cause);
                };
            }]);

        }]);
})();


/**
 * simple service to handle our errors
 */
(function() {
    'use strict';
    ApplicationError.$inject = ['$rootScope'];
    angular.module('moaClientApp')
      .service("ApplicationError", ApplicationError);

    /* @ngInject */
    function ApplicationError($rootScope) {
        this.handleError = function(message, reason) {
            $rootScope.addError({message: message, reason: reason})

        };

        this.clearErrors = function() {
            $rootScope.clearErrors();
        }
    }
})();
