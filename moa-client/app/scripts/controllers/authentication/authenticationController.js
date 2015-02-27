/**
 * Created by wohlgemuth on 7/11/14.
 */
moaControllers.AuthenticationController = function ($scope, $rootScope, $modal, AuthenticationService) {
    /**
     * Returns whether or not the user is logged in
     * @returns {*}
     */
    $scope.isLoggedIn = function() {
        return AuthenticationService.isLoggedIn();
    };

    /**
     * Handle login
     */
    $scope.handleLogin = function() {
        if ($scope.isLoggedIn()) {
            AuthenticationService.logout();
        } else {
            $scope.openAuthenticationDialog();
        }
    };

    /**
     * Opens the authentication modal dialog
     */
    $scope.openAuthenticationDialog = function () {
        var modalInstance = $modal.open({
            templateUrl: '/views/authentication/authenticationModal.html',
            controller: moaControllers.AuthenticationModalController,
            size: 'sm',
            backdrop: 'true'
        });
    };

    $scope.$on('auth:login-success', function(event, data, status, headers, config) {
        AuthenticationService.getCurrentUser().then(function(data) {
            $scope.name = data.firstName;
        });
    });
};


moaControllers.AuthenticationModalController = function ($scope, $rootScope, $modalInstance, $timeout, AuthenticationService) {
    $scope.errors = [];
    $scope.state = 'login';

    // Temporary
    $scope.email = 'wohlgemuth@ucdavis.edu';
    $scope.password = 'password';


    $scope.cancelDialog = function () {
        $modalInstance.dismiss('cancel');
    };

    /**
     * closes the dialog and finishes and builds the query
     */
    $scope.submitLogin = function () {
        $scope.errors = [];

        if ($scope.email == '') {
            $scope.errors.push('Please enter your email address');
        }

        if ($scope.password == '') {
            $scope.errors.push('Please enter your password');
        }

        if($scope.errors.length == 0) {
            $scope.state = 'logging in';
            AuthenticationService.login($scope.email, $scope.password);
        }
    };

    $scope.$on('auth:login-success', function(event, data, status, headers, config) {
        $scope.state = 'successful';
        $timeout(function() {
            $modalInstance.close();
        }, 2000);
    });

    $scope.$on('auth:login-error', function(event, data, status, headers, config) {
        $scope.state = 'login';
        $scope.errors.push('Invalid email or password');
    });
};