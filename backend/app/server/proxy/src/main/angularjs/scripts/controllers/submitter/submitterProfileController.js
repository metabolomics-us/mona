/**
 * Created by sajjan on 4/24/15.
 */

(function() {
    'use strict';
    SubmitterProfileController.$inject = ['$scope', 'AuthenticationService', 'SpectraQueryBuilderService'];
    angular.module('moaClientApp')
        .controller('SubmitterProfileController', SubmitterProfileController);

    /* @ngInject */
    function SubmitterProfileController($scope, AuthenticationService, SpectraQueryBuilderService) {

        function setUserData() {
            AuthenticationService.getCurrentUser().then(function(data) {
                $scope.user = data;
            });
        }

        $scope.$on('auth:login-success', setUserData);
        $scope.$on('auth:user-update', setUserData);
        setUserData();

        /**
         * Executes a new query based on username
         */
        $scope.queryUserSpectra = function() {
            SpectraQueryBuilderService.prepareQuery();
            SpectraQueryBuilderService.addUserToQuery($scope.user.username);
            SpectraQueryBuilderService.executeQuery();
        };
    }
})();
