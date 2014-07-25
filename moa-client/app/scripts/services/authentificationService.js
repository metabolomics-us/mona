/**
 * Created by wohlgemuth on 6/9/14.
 */

/**
 * a service to handle authentifications and provides us with the currently logged in user
 */
app.service('AuthentificationService', function (Submitter, $q,$rootScope) {

    /**
     * log us in
     */
    this.login = function () {

        $rootScope.currentUser = null;


        Submitter.get({id: 1}, function (data) {
            $rootScope.currentUser = data;
        });

    };

    /**
     * log us out
     */
    this.logout = function () {
        //doesn't do anything yet
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