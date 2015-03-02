/**
 * Created by sajjan on 3/2/15.
 */

'use strict';

moaControllers.NavigationController = function($scope, $rootScope, AuthenticationService) {
    var ADMIN_ROLE_NAME = 'ROLE_ADMIN';


    /**
     * Returns whether or not the user is logged in
     * @returns {*}
     */
    $scope.isLoggedIn = function() {
        return AuthenticationService.isLoggedIn();
    };

    $scope.isAdmin = function() {
        if (AuthenticationService.isLoggedIn()) {
            for (var i = 0; i < $rootScope.currentUser.roles.length; i++) {
                if ($rootScope.currentUser.roles[i].authority == ADMIN_ROLE_NAME)
                    return true;
            }
        }

        return false;
    }
};