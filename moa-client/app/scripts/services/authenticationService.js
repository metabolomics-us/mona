/**
 * Created by wohlgemuth on 6/9/14.
 */

/**
 * a service to handle authentications and provides us with the currently logged in user
 */
app.service('AuthenticationService', function (Submitter, $q, $resource, $rootScope) {

    /**
     * log us in
     */
    this.login = function (emailAddress, password) {
        $rootScope.currentUser = null;

        Submitter.get({id: 1}, function (data) {
            $rootScope.currentUser = data;
        });
    };

    /**
     * validate user
     */
    this.validate = function () {
        if (angular.isDefined($rootScope.currentUser)) {
            $resource(REST_BACKEND_SERVER +'/rest/login/validate', {}, {
                post: {
                    method: 'POST',
                    headers: {'X-Auth-Token': $rootScope.currentUser.access_token}
                }
            }).post({})
        } else {
            return false;
        }
    };

    /**
     * log us out
     */
    this.logout = function () {
        //doesn't do anything yet
    };


    this.isLoggedIn = function() {
        return angular.isDefined($rootScope.currentUser);
    };

    /**
     * returns a promise of the currently logged in user
     * @returns {*}
     */
    this.getCurrentUser = function () {
        var deferred = $q.defer();

        deferred.resolve($rootScope.currentUser);

        return deferred.promise
    };

    /**
     * just auto login our user
     */
    this.login();
});