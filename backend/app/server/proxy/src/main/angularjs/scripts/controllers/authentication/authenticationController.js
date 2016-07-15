/**
 * Created by wohlgemuth on 7/11/14.
 */

// TODO: waiting for implementation of return user data for admin from authentication Service

(function() {
    'use strict';
    AuthenticationController.$inject = ['$scope', '$rootScope', '$uibModal', 'AuthenticationService'];
    angular.module('moaClientApp')
      .controller('AuthenticationController', AuthenticationController);


    /* @ngInject */
    function AuthenticationController($scope, $rootScope, $uibModal, AuthenticationService) {
        var ADMIN_ROLE_NAME = 'ROLE_ADMIN';
        var self = this;

        self.currentUser = null;
        self.welcomeMessage = 'Login/Register';


        /**
         * Returns whether or not the user is logged in
         * @returns {*}
         */
        self.isLoggedIn = function() {
            return AuthenticationService.isLoggedIn();
        };

        self.isAdmin = function() {
            if (AuthenticationService.isLoggedIn() && angular.isDefined($rootScope.currentUser.roles)) {
                for (var i = 0; i < $rootScope.currentUser.roles.length; i++) {
                    if ($rootScope.currentUser.roles[i].authority === ADMIN_ROLE_NAME)
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
        self.openAuthenticationDialog = function() {
            $uibModal.open({
                templateUrl: '/views/authentication/authenticationModal.html',
                controller: 'AuthenticationModalController',
                size: 'sm',
                backdrop: 'true'
            });
        };

        /**
         * Opens the registration modal dialog
         */
        self.handleRegistration = function() {
            if (!self.isLoggedIn()) {
                $uibModal.open({
                    templateUrl: '/views/authentication/registrationModal.html',
                    controller: 'RegistrationModalController',
                    size: 'md',
                    backdrop: 'static'
                });
            }
        };

        /**
         * Create a welcome message on login
         */
        $scope.$on('auth:login-success', function(event, data, status, headers, config) {

            AuthenticationService.getCurrentUser().then(function(data) {
                console.log(JSON.stringify(data));
                self.welcomeMessage = 'Welcome, ' + data.username + '!';
            });
        });

        /**
         * Remove the welcome message on logout
         */
        $scope.$on('auth:logout', function(event, data, status, headers, config) {
            self.welcomeMessage = 'Login/Register';
        });

        /**
         * Listen for external calls to bring up the authentication modal
         */
        $scope.$on('auth:login', function(event) {
            if (!self.isLoggedIn()) {
                self.openAuthenticationDialog();
            }
        });

        /**
         * Attempt to log in with authentication cookie stored in cookie
         */
        (function() {
            AuthenticationService.validate();
        })();
    }
})();



