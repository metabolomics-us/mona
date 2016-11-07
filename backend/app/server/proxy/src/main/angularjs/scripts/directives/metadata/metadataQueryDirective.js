/**
 * links a metadata field to a query builder and executes the spectra query for us
 */

(function() {
    'use strict';

    metadataQueryController.$inject = ['$scope', 'SpectraQueryBuilderService', '$location', '$log'];
    angular.module('moaClientApp')
        .directive('metadataQuery', metadataQuery);

    function metadataQuery() {
        var directive = {
            restrict: 'A',
            templateUrl: '/views/templates/meta/metadataQuery.html',
            replace: true,
            transclude: true,
            scope: {
                metaData: '=value',
                compound: '=compound'
            },
            controller: metadataQueryController
        };

        return directive;
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
            } else {
                SpectraQueryBuilderService.addMetaDataToQuery($scope.metaData.name, $scope.metaData.value);
            }

            SpectraQueryBuilderService.executeQuery();
        };
    }
})();
