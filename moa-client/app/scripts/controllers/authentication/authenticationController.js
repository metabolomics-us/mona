/**
 * Created by wohlgemuth on 7/11/14.
 */
'use strict';

moaControllers.AuthenticationController = function ($scope, $rootScope, $modal, AuthenticationService) {
    var ADMIN_ROLE_NAME = 'ROLE_ADMIN';
    var self = this;

    self.currentUser = null;
    self.welcomeMessage = '';


    /**
     * Returns whether or not the user is logged in
     * @returns {*}
     */
    self.isLoggedIn = function() {
        return AuthenticationService.isLoggedIn();
    };

    self.isAdmin = function() {
        if (AuthenticationService.isLoggedIn()) {
            for (var i = 0; i < $rootScope.currentUser.roles.length; i++) {
                if ($rootScope.currentUser.roles[i].authority == ADMIN_ROLE_NAME)
                    return true;
            }
        }

        return false;
    };

    /**
     * Handle login
     */
    self.handleLogin = function() {
        if (self.isLoggedIn()) {
            AuthenticationService.logout();
        } else {
            self.openAuthenticationDialog();
        }
    };

    /**
     * Opens the authentication modal dialog
     */
    self.openAuthenticationDialog = function () {
        var modalInstance = $modal.open({
            templateUrl: '/views/authentication/authenticationModal.html',
            controller: moaControllers.AuthenticationModalController,
            size: 'sm',
            backdrop: 'true'
        });
    };

    /**
     * Create a welcome message on login
     */
    $scope.$on('auth:login-success', function(event, data, status, headers, config) {
        AuthenticationService.getCurrentUser().then(function(data) {
            self.welcomeMessage = "Welcome "+ data.firstName +"!";
        });
    });

    /**
     * Remove the welcome message on logout
     */
    $scope.$on('auth:logout', function(event, data, status, headers, config) {
        self.welcomeMessage = '';
    });

    /**
     * Listen for external calls to bring up the authentication modal
     */
    $scope.$on('auth:login', function(event) {
        if (self.isLoggedIn()) {
            self.openAuthenticationDialog();
        }
    });

    /**
     * Attempt to log in with authentication cookie stored in cookie
     */
    (function() {
        AuthenticationService.validate();
    })();
};


moaControllers.AuthenticationModalController = function ($scope, $rootScope, $modalInstance, $timeout, AuthenticationService) {
    $scope.errors = [];
    $scope.state = 'login';

    $scope.credentials = {
        email: 'wohlgemuth@ucdavis.edu',
        password: 'password'
    };

    $scope.cancelDialog = function () {
        $modalInstance.dismiss('cancel');
    };

    /**
     * closes the dialog and finishes and builds the query
     */
    $scope.submitLogin = function () {
        $scope.errors = [];

        if ($scope.credentials.email == '') {
            $scope.errors.push('Please enter your email address');
        }

        if ($scope.credentials.password == '') {
            $scope.errors.push('Please enter your password');
        }

        if($scope.errors.length == 0) {
            $scope.state = 'logging in';
            AuthenticationService.login($scope.credentials.email, $scope.credentials.password);
        }
    };

    $scope.$on('auth:login-success', function(event, data, status, headers, config) {
        $scope.state = 'success';
        $timeout(function() {
            $modalInstance.close();
        }, 1000);
    });

    $scope.$on('auth:login-error', function(event, data, status, headers, config) {
        $scope.state = 'login';
        $scope.errors.push('Invalid email or password');
    });
};