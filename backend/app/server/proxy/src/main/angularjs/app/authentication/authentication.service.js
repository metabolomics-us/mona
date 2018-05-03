/**
 * Created by wohlgemuth on 6/9/14.
 * a service to handle authentications and provides us with the currently logged in user
 */

(function() {
    'use strict';

    authenticationService.$inject = ['Submitter', '$log', '$q', '$http', '$rootScope', 'CookieService', 'REST_BACKEND_SERVER'];
    angular.module('moaClientApp')
        .service('AuthenticationService', authenticationService);

    /* @ngInject */
    function authenticationService(Submitter, $log, $q, $http, $rootScope, CookieService, REST_BACKEND_SERVER) {
        var self = this;
        self.loggingIn = false;

        function pullSubmitterData() {
            $http({
                method: 'GET',
                url: REST_BACKEND_SERVER + '/rest/submitters/'+ $rootScope.currentUser.username,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer '+ $rootScope.currentUser.access_token
                }
            }).then(function (response) {
                $rootScope.currentUser.emailAddress = response.data.emailAddress;
                $rootScope.currentUser.firstName = response.data.firstName;
                $rootScope.currentUser.lastName = response.data.lastName;
                $rootScope.currentUser.institution = response.data.institution;
                $rootScope.$broadcast('auth:user-update', $rootScope.currentUser);
            });
        }

        /**
         * log us in
         */
        this.login = function(userName, password) {
            self.loggingIn = true;

	        $http({
                method: 'POST',
                url: REST_BACKEND_SERVER + '/rest/auth/login',
                headers: {
                    'Content-Type': 'application/json'
                },
                data: {username: userName, password: password}
            }).then(
                function (response) {
                    var token = response.data.token;

                    $rootScope.currentUser = {username: response.config.data.username, access_token: token};
                    $log.info("Login success.  Current token: "+ $rootScope.currentUser.access_token);

                    CookieService.update('AuthorizationToken', token);
                    $rootScope.$broadcast('auth:login-success', token, response.status, response.headers, response.config);
                    self.loggingIn = false;

                    pullSubmitterData();
                },
                function (response) {
                    $log.info(response);
                    $rootScope.$broadcast('auth:login-error', response.data, response.status, response.headers, response.config);
                    self.loggingIn = false;
                }
            );
        };

        /**
         * validate user
         */
        this.validate = function() {
            var access_token = undefined;
            self.loggingIn = true;

            if (this.isLoggedIn()) {
                access_token = $rootScope.currentUser.access_token;
                $log.info("Validation: logged in with token: "+ access_token);
            } else {
                access_token = CookieService.get('AuthorizationToken');
                $log.info("Validation: getting token from cookie: "+ access_token);
            }

            // Only try validating if we found a stored token
            if (angular.isDefined(access_token) && access_token != null && access_token != "") {
                $http({
                    method: 'POST',
                    url: REST_BACKEND_SERVER + '/rest/auth/info',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer '+ access_token
                    },
                    data: {
                        token: access_token
                    }
                }).then(
                    function (response) {
                        $log.info(response);
                        $rootScope.currentUser = {username: response.data.username, access_token: response.config.data.token};
                        $log.info("Validation successful");
                        pullSubmitterData();
                    },
                    function (response) {
                        $rootScope.currentUser = null;
                        CookieService.remove('AuthorizationToken');
                        $rootScope.$broadcast('auth:login-error', response.data, response.status, response.headers, response.config);
                        self.loggingIn = false;
                    }
                );

            } else {
                self.loggingIn = false;
            }
        };

        /**
         * log us out
         */
        this.logout = function() {
            $rootScope.$broadcast('auth:logout', null, null, null, null);
            $rootScope.currentUser = null;
            CookieService.remove('AuthorizationToken');
        };


        this.isLoggedIn = function() {
            return angular.isDefined($rootScope.currentUser) &&
              $rootScope.currentUser !== null &&
              angular.isDefined($rootScope.currentUser.access_token);
        };

        this.isLoggingIn = function() {
            return self.loggingIn;
        };

        /**
         * returns a promise of the currently logged in user
         * @returns {*}
         */
        this.getCurrentUser = function() {
            var deferred = $q.defer();

            if (this.isLoggedIn()) {
                deferred.resolve($rootScope.currentUser);
            } else {
                deferred.resolve({});
            }

            return deferred.promise;
        };
    }
})();