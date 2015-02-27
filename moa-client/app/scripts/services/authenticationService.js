/**
 * Created by wohlgemuth on 6/9/14.
 */

/**
 * a service to handle authentications and provides us with the currently logged in user
 */
app.service('AuthenticationService', function (Submitter, $q, $http, $resource, $rootScope, REST_BACKEND_SERVER) {
    /**
     * log us in
     */
    this.login = function (emailAddress, password) {
        $resource(REST_BACKEND_SERVER +'/rest/login',
            {email: "@email", password: "@password"}, {
            post: {
                method: 'POST'
            }
        }).post({
            email: emailAddress,
            password: password
        }, function(data, status, headers, config) {
            $rootScope.currentUser = data;
            $http.defaults.headers.common['Authorization'] = data.access_token;
            $http.defaults.headers.common['X-Auth-Token'] = data.access_token;

            $rootScope.$broadcast('auth:login-success', data, status, headers, config);
        }, function(data, status, headers, config) {
            $rootScope.$broadcast('auth:login-error', data, status, headers, config);
        });
    };

    /**
     * validate user
     */
    this.validate = function () {
        if (this.isLoggedIn()) {
            $resource(REST_BACKEND_SERVER +'/rest/login/validate', {}, {
                post: {
                    method: 'POST'
                }
            }).post({}, function(data, status, headers, config) {
                $rootScope.$broadcast('auth:validate-success', data, status, headers, config);
            }, function(data, status, headers, config) {
                $rootScope.$broadcast('auth:validate-error', data, status, headers, config);
            });
        } else {
            $rootScope.$broadcast('auth:login-status', null, null, null, null);
        }
    };

    /**
     * log us out
     */
    this.logout = function () {
        if (this.isLoggedIn()) {
            $resource(REST_BACKEND_SERVER +'/rest/logout', {}, {
                post: {
                    method: 'POST'
                }
            }).post({}, function(data, status, headers, config) {
                $rootScope.$broadcast('auth:logout-success', data, status, headers, config);
            }, function(data, status, headers, config) {
                $rootScope.$broadcast('auth:logout-error', data, status, headers, config);
            });

            $rootScope.currentUser = null;
            $http.defaults.headers.common['Authorization'] = undefined;
            $http.defaults.headers.common['X-Auth-Token'] = undefined;
        } else {
            $rootScope.$broadcast('auth:logout-status', null, null, null, null);
        }
    };


    this.isLoggedIn = function() {
        return angular.isDefined($rootScope.currentUser) &&
               $rootScope.currentUser != null &&
               angular.isDefined($rootScope.currentUser.access_token);
    };

    /**
     * returns a promise of the currently logged in user
     * @returns {*}
     */
    this.getCurrentUser = function () {
        if (this.isLoggedIn()) {
            var deferred = $q.defer();

            deferred.resolve($rootScope.currentUser);

            return deferred.promise
        } else {
            return null;
        }
    };
});