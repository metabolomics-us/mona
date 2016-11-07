/**
 * Creates or updates a query based on SPLASH
 */

(function() {
    'use strict';

    splashQueryController.$inject = ['$scope', 'SpectraQueryBuilderService'];
    angular.module('moaClientApp')
        .directive('splashQuery', splashQuery);

    function splashQuery() {
        return {
            restrict: 'A',
            templateUrl: '/views/templates/query/splashQuery.html',
            replace: true,
            transclude: true,
            scope: {
                value: '=value'
            },
            controller: splashQueryController
        };
    }

    /* @ngInject */
    function splashQueryController($scope, SpectraQueryBuilderService) {
        /**
         * Create a new query based on the selected SPLASH
         */
        $scope.newQuery = function() {
            SpectraQueryBuilderService.prepareQuery();
            $scope.addToQuery();
        };

        /**
         * Add selected SPLASH to the current query
         */
        $scope.addToQuery = function() {
            SpectraQueryBuilderService.addSplashToQuery($scope.value.splash);
            SpectraQueryBuilderService.executeQuery();
        };
    }
})();