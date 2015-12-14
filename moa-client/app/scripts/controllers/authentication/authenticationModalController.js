(function() {
    'use strict';
    angular.module('moaClientApp')
      .controller('AuthenticationModalController', AuthenticationModalController)

    AuthenticationModalController.$inject = ['$scope', '$rootScope', '$uibModalInstance', '$timeout', 'AuthenticationService'];

    function AuthenticationModalController($scope, $rootScope, $uibModalInstance, $timeout, AuthenticationService) {
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

            if (data.status == '401') {
                $scope.errors.push('Invalid email or password');
            } else {
                $scope.errors.push('Unable to reach MoNA server');
            }
        });
    }
})();