/**
 * Created by wohlgemuth on 6/9/14.
 * a service to handle authentications and provides us with the currently logged in user
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .factory('AuthenticationService', ['Submitter', '$q', '$http', '$resource', '$rootScope', 'CookieService', 'REST_BACKEND_SERVER',
          function(Submitter, $q, $http, $resource, $rootScope, CookieService, REST_BACKEND_SERVER) {
              var self = this;
              self.loggingIn = false;

              var handleLoginSuccess = function(response) {
                  // In progress to handle errors more gracefully
                  if (response.status === 200) {

                  }
                  $rootScope.currentUser = response.data;
                  $http.defaults.headers.common['X-Auth-Token'] = response.data.access_token;
                  CookieService.update('AuthorizationToken', response.data.access_token);

                  $rootScope.$broadcast('auth:login-success', data, status, headers, config);
              };

              /**
               * log us in
               */
              this.login = function(emailAddress, password) {
                  self.loggingIn = true;

                  $resource(REST_BACKEND_SERVER + '/rest/login', {}, {
                      post: {
                          method: 'POST',
                          headers: {
                              'Content-Type': 'application/json'
                          }
                      }
                  }).post({
                      email: emailAddress,
                      password: password
                  }, function(data, status, headers, config) {
                      $rootScope.currentUser = data;
                      $http.defaults.headers.common['X-Auth-Token'] = data.access_token;
                      CookieService.update('AuthorizationToken', data.access_token);

                      $rootScope.$broadcast('auth:login-success', data, status, headers, config);
                      self.loggingIn = false;
                  }, function(data, status, headers, config) {
                      $rootScope.$broadcast('auth:login-error', data, status, headers, config);
                      self.loggingIn = false;
                  });
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

                      $resource(REST_BACKEND_SERVER + '/rest/login/validate', {}, {
                          post: {
                              method: 'POST'
                          }
                      }).post({}, function(data, status, headers, config) {
                          $rootScope.currentUser = data;
                          $http.defaults.headers.common['X-Auth-Token'] = data.access_token;
                          CookieService.update('AuthorizationToken', data.access_token);

                          $rootScope.$broadcast('auth:login-success', data, status, headers, config);
                          self.loggingIn = false;
                      }, function(data, status, headers, config) {
                          $rootScope.currentUser = null;
                          $http.defaults.headers.common['X-Auth-Token'] = undefined;
                          CookieService.remove('AuthorizationToken');

                          $rootScope.$broadcast('auth:login-error', data, status, headers, config);
                          self.loggingIn = false;
                      });
                  } else
                      self.loggingIn = false;
              };

              /**
               * log us out
               */
              this.logout = function() {
                  if (this.isLoggedIn()) {
                      $resource(REST_BACKEND_SERVER + '/rest/logout', {}, {
                          post: {
                              method: 'POST'
                          }
                      }).post({}, function(data, status, headers, config) {
                          $rootScope.$broadcast('auth:logout', data, status, headers, config);
                      }, function(data, status, headers, config) {
                          $rootScope.$broadcast('auth:logout', data, status, headers, config);
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
          }]);
})();