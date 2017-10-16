/**
 * Creates or updates a query with the given submitter information
 */

(function() {
    'use strict';

    submitterQueryController.$inject = ['$scope', 'SpectraQueryBuilderService'];
    angular.module('moaClientApp')
        .directive('submitterQuery', submitterQuery);

    function submitterQuery() {
        return {
            replace: true,
            transclude: true,
            templateUrl: '/views/templates/query/submitterQuery.html',
            restrict: 'A',
            scope: {
                submitter: '=submitter'
            },
            controller: submitterQueryController
        };
    }

    /* @ngInject */
    function submitterQueryController($scope, SpectraQueryBuilderService) {
        /**
         * Create a new query based on the selected submitter
         */
        $scope.newQuery = function() {
            SpectraQueryBuilderService.prepareQuery();
            $scope.addToQuery();
        };

        /**
         * Add selected submitter to the current query
         */
        $scope.addToQuery = function() {
            SpectraQueryBuilderService.addUserToQuery($scope.submitter.id);
            SpectraQueryBuilderService.executeQuery();
        };

        /**
         * Curate spectra based on selected submitter
         */
        $scope.curateSpectra = function() {
            SpectraQueryBuilderService.prepareQuery();
            SpectraQueryBuilderService.addUserToQuery($scope.submitter.id);

            var query = SpectraQueryBuilderService.getRSQLQuery();
            // TODO Add curation functionality
            // Spectrum.curateSpectraByQuery(query, function(data) {});
        }
    }
})();