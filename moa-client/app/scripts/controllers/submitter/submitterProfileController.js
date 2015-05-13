/**
 * Created by sajjan on 4/24/15.
 */
'use strict';

moaControllers.SubmitterProfileController = function ($scope, $location, AuthenticationService, SpectraQueryBuilderService) {
    $scope.$on('auth:login-success', function(event, data, status, headers, config) {
        AuthenticationService.getCurrentUser().then(function(data) {
            $scope.user = data;
        });
    });

    (function() {
        AuthenticationService.getCurrentUser().then(function(data) {
            $scope.user = data;
        });
    })();


    $scope.queryUserSpectra = function() {
        SpectraQueryBuilderService.compileQuery({ submitter: $scope.user.emailAddress });
        $location.path('/spectra/browse/');
    };
};