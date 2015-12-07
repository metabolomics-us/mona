/**
 * Created by wohlgemuth on 7/11/14.
 */

(function() {
    'use strict';
    moaControllers.AuthenticationController = ['$scope', '$rootScope', '$uibModal', 'AuthenticationService',
        function($scope, $rootScope, $uibModal, AuthenticationService) {
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
                if (AuthenticationService.isLoggedIn()) {
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
                    controller: moaControllers.AuthenticationModalController,
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
                        controller: moaControllers.RegistrationModalController,
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
                    self.welcomeMessage = 'Welcome, ' + data.firstName + '!';
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
        }];


    moaControllers.AuthenticationModalController = ['$scope', '$rootScope', '$uibModalInstance', '$timeout', 'AuthenticationService',
        function($scope, $rootScope, $uibModalInstance, $timeout, AuthenticationService) {
            $scope.errors = [];
            $scope.state = 'login';

            $scope.credentials = {
                email: '',
                password: ''
            };

            $scope.cancelDialog = function() {
                $uibModalInstance.dismiss('cancel');
            };

            /**
             * closes the dialog and finishes and builds the query
             */
            $scope.submitLogin = function() {
                $scope.errors = [];

                if ($scope.credentials.email === '') {
                    $scope.errors.push('Please enter your email address');
                }

                if ($scope.credentials.password === '') {
                    $scope.errors.push('Please enter your password');
                }

                if ($scope.errors.length === 0) {
                    $scope.state = 'logging in';
                    AuthenticationService.login($scope.credentials.email, $scope.credentials.password);
                }
            };

            $scope.$on('auth:login-success', function(event, data, status, headers, config) {
                $scope.state = 'success';
                $timeout(function() {
                    $uibModalInstance.close();
                }, 1000);
            });

            $scope.$on('auth:login-error', function(event, data, status, headers, config) {
                $scope.state = 'login';

                if (data.status === '401') {
                    $scope.errors.push('Invalid email or password');
                } else {
                    $scope.errors.push('Unable to reach MoNA server');
                }
            });
        }];


    moaControllers.RegistrationModalController = ['$scope', '$rootScope', '$uibModalInstance', 'Submitter',
        function($scope, $rootScope, $uibModalInstance, Submitter) {
            $scope.errors = [];
            $scope.state = 'register';

            $scope.newSubmitter = {};

            $scope.cancelDialog = function() {
                $uibModalInstance.dismiss('cancel');
            };

            /**
             * closes the dialog and finishes and builds the query
             */
            $scope.submitRegistration = function() {
                $scope.errors = [];

                var submitter = new Submitter();
                submitter.firstName = $scope.newSubmitter.firstName;
                submitter.lastName = $scope.newSubmitter.lastName;
                submitter.institution = $scope.newSubmitter.institution;
                submitter.emailAddress = $scope.newSubmitter.emailAddress;
                submitter.password = $scope.newSubmitter.password;

                $scope.state = 'registering';

                Submitter.save(submitter,
                  function() {
                      $scope.state = 'success';
                  },
                  function(data) {
                      $scope.state = 'register';

                      if (data.status === 422) {
                          for (var i = 0; i < data.data.errors.length; i++) {
                              var message = 'Error in ' + data.data.errors[i].field + ': ';

                              if (data.data.errors[i].message.indexOf('must be unique') > -1) {
                                  $scope.errors.push(message + 'already exists!');
                              } else {
                                  $scope.errors.push(message + data.data.errors[i].message);
                              }
                          }
                      } else {
                          $scope.errors.push('An unknown error has occurred: ' + JSON.stringify(data));
                      }
                  }
                );
            };

            /**
             * Close dialog and open login modal
             */
            $scope.logIn = function() {
                $uibModalInstance.dismiss('cancel');
                $rootScope.$broadcast('auth:login');
            };
        }];
})();
