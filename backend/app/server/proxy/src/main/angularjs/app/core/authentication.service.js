'use strict';

export default class AuthenticationService {

  constructor($rootScope, $http, $q, $log, Submitter, CookieService) {
    this.loggingIn = false;
  }


  isLoggedIn() {
    return angular.isDefined($rootScope.currentUser) &&
      $rootScope.currentUser !== null &&
      angular.isDefined($rootScope.currentUser.access_token);
  }

  isLoggingIn() {
    return this.loggingIn;
  }

  login() {
    this.loggingIn = true;

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
        this.loggingIn = false;

        pullSubmitterData();
      },
      function (response) {
        $log.info(response);
        $rootScope.$broadcast('auth:login-error', response.data, response.status, response.headers, response.config);
        this.loggingIn = false;
      }
    );
  }

  pullSubmitterData() {
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

  validate() {
    var access_token = undefined;
    this.loggingIn = true;

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
          this.loggingIn = false;
        }
      );

    } else {
      this.loggingIn = false;
    }
  }

  logout() {
    $rootScope.$broadcast('auth:logout', null, null, null, null);
    $rootScope.currentUser = null;
    CookieService.remove('AuthorizationToken');
  }

  getCurrentUser() {
    var deferred = $q.defer();

    if (isLoggedIn()) {
      deferred.resolve($rootScope.currentUser);
    } else {
      deferred.resolve();
    }

    return deferred;
  }
}

AuthenticationService.$inject = ['$rootScope', '$http', '$q', '$log', 'Submitter', 'CookieService'];
