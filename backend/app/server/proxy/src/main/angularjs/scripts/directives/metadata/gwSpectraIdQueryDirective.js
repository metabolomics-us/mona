/**
 * adds the given id to the query or removes it
 */

(function() {
    'use strict';

    gwSpectraIdQueryController.$inject = ['$scope', '$element', 'SpectraQueryBuilderService', '$location'];
    angular.module('moaClientApp')
      .directive('gwSpectraIdQuery', gwSpectraIdQuery)

    function gwSpectraIdQuery() {
        var directive = {
            restrict: 'A',
            templateUrl: '/views/templates/spectra/spectraHashQuery.html',
            replace: true,
            transclude: true,
            scope: {
                value: '=value'
            },
            link: linkFunc,
            controller: gwSpectraIdQueryController
        };

        return directive;
    }

    function linkFunc($scope, element, attrs, ngModel) {

    }

    //controller to handle building new queries
    /* @ngInject */
    function gwSpectraIdQueryController($scope, $element, SpectraQueryBuilderService, $location) {

        //receive a click
        $scope.newQuery = function() {
            //build a mona query based on this label
            SpectraQueryBuilderService.prepareQuery();

            //add it to query
            SpectraQueryBuilderService.addSpectraIdToQuery($scope.value.hash);

            //assign to the cache

            //run the query and show it's result in the spectra browser
            $location.path("/spectra/browse/");
        };

        //receive a click
        $scope.addToQuery = function() {
            SpectraQueryBuilderService.addSpectraIdToQuery($scope.value.hash);
            $location.path("/spectra/browse/");
        };

        //finds related spectra to this spectra
        $scope.findSimilarSpectra = function() {
            SpectraQueryBuilderService.prepareQuery();

            SpectraQueryBuilderService.addSimilarSpectraToQuery($scope.value.hash, $scope.value.spectrum);
            $location.path("/spectra/browse/");
        };

        //receive a click
        $scope.removeFromQuery = function() {
            //build a mona query based on this label
            SpectraQueryBuilderService.removeSpectraIdFromQuery($scope.value.hash);

            //run the query and show it's result in the spectra browser
            $location.path("/spectra/browse/");
        };
    }
})();