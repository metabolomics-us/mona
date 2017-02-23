/**
 * links a metadata field to a query builder and executes the spectra query for us
 */

(function() {
    'use strict';

    metadataQueryController.$inject = ['$scope', 'SpectraQueryBuilderService', '$location', '$log'];
    angular.module('moaClientApp')
        .directive('metadataQuery', metadataQuery);

    function metadataQuery() {
        return {
            restrict: 'A',
            templateUrl: '/views/templates/query/metadataQuery.html',
            replace: true,
            transclude: true,
            scope: {
                metaData: '=value',
                compound: '=compound',
                classification: '=classification'
            },
            controller: metadataQueryController
        };
    }

    /* @ngInject */
    function metadataQueryController($scope, SpectraQueryBuilderService, $location, $log) {
        /**
         * Create a new query based on the selected metadata value
         */
        $scope.newQuery = function() {
            SpectraQueryBuilderService.prepareQuery();
            $scope.addToQuery();
        };

        /**
         * Add selected metadata value to the current query
         */
        $scope.addToQuery = function() {
            if (angular.isDefined($scope.compound)) {
                SpectraQueryBuilderService.addCompoundMetaDataToQuery($scope.metaData.name, $scope.metaData.value);
            } else if (angular.isDefined($scope.classification)) {
                SpectraQueryBuilderService.addClassificationToQuery($scope.metaData.name, $scope.metaData.value);
            } else {
                SpectraQueryBuilderService.addMetaDataToQuery($scope.metaData.name, $scope.metaData.value);
            }

            SpectraQueryBuilderService.executeQuery();
        };
    }
})();
