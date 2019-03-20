/**
 * Creates or updates a query with the given submitter information
 */

(function() {
    'use strict';

    spectrumReviewController.$inject = ['$scope'];
    angular.module('moaClientApp')
        .directive('spectrumReview', spectrumReview);

    function spectrumReview() {
        return {
            replace: true,
            templateUrl: '/views/templates/feedback/submitterQuery.html',
            restrict: 'A',
            scope: {
                spectrum: '=spectrum'
            },
            controller: spectrumReviewController
        };
    }

    /* @ngInject */
    function spectrumReviewController($scope) {

        $scope.rate = function(value) {

        };

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