/**
 * Created by wohlgemuth on 6/9/14.
 */

/**
 * a service to handle authentifications and provides us with the currently logged in user
 */
app.service('AuthentificationService', function (Submitter, $q) {

    /**
     * log us in
     */
    this.login = function () {
        //doesn't do anything yet
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

        //dummy function to return user number 1
        var deferred = $q.defer();

        Submitter.get({id: 1}, function (data) {
            deferred.resolve(data);
        });

        return deferred.promise;

    }
});