/**
 * Created by wohlgemuth on 6/9/14.
 * a service to handle authentications and provides us with the currently logged in user
 */


// TODO: waiting for implementation of return user data for admin
(function() {
    'use strict';
    angular.module('moaClientApp')
      .service('AuthenticationService', authenticationService);

    /* @ngInject */
    function authenticationService(Submitter, $log, $q, $http, $rootScope, CookieService, REST_BACKEND_SERVER) {
        var self = this;
        self.loggingIn = false;

        function handleLoginSuccess(response) {
            var token = response.data.token;
            $log.info(response);
            $rootScope.currentUser = {username: response.config.data.username, access_token: token};
            $http.defaults.headers.common['X-Auth-Token'] = token;
            CookieService.update('AuthorizationToken', token);
            $rootScope.$broadcast('auth:login-success', token, response.status, response.headers, response.config);
            self.loggingIn = false;
        }

        function handleLoginFail(response) {
            $rootScope.$broadcast('auth:login-error', response.data, response.status, response.headers, response.config);
            self.loggingIn = false;
        }

        function handleValidationFail(response) {
            $rootScope.currentUser = null;
            $http.defaults.headers.common['X-Auth-Token'] = undefined;
            CookieService.remove('AuthorizationToken');
            $rootScope.$broadcast('auth:login-error', response.data, response.status, response.headers, response.config);
            self.loggingIn = false;
        }

        /**
         * log us in
         */
        this.login = function(userName, password) {
            self.loggingIn = true;

            $http.post(REST_BACKEND_SERVER + '/rest/auth/login',
              {username: userName, password: password},
              {headers: {'Content-Type': 'application/json'}}).then(handleLoginSuccess, handleLoginFail);
        };

        /**
         * validate user
         */
        this.validate = function() {
            var access_token = undefined;
            self.loggingIn = true;

            if (this.isLoggedIn()) {
                access_token = $rootScope.currentUser.access_token;
            } else {
                access_token = CookieService.get('AuthorizationToken');
            }

            if (angular.isDefined(access_token)) {
                $http.defaults.headers.common['X-Auth-Token'] = access_token;
                $http.post(REST_BACKEND_SERVER + '/rest/auth/validate', {}).then(handleLoginSuccess, handleValidationFail);

            } else
                self.loggingIn = false;
        };

        /**
         * log us out
         */
        this.logout = function() {
            if (this.isLoggedIn()) {
                $http.post(REST_BACKEND_SERVER + '/rest/auth/logout', {})
                  .then(function success(response) {
                      $rootScope.$broadcast('auth:logout', response.data, response.status, response.headers, response.config);
                  }, function fail(response) {
                      $rootScope.$broadcast('auth:logout', response.data, response.status, response.headers, response.config);
                  });
            } else {
                $rootScope.$broadcast('auth:logout', null, null, null, null);
            }

            $rootScope.currentUser = null;
            $http.defaults.headers.common['X-Auth-Token'] = undefined;
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
            if (this.isLoggedIn()) {
                var deferred = $q.defer();

                deferred.resolve($rootScope.currentUser);

                return deferred.promise
            } else {
                return null;
            }
        };

    }
})();