/**
 * links a metadata field to a query builder and executes the spectra query for us
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('gwMetaQuery', gwMetaQuery);

    function gwMetaQuery() {
        var directive = {
            restrict: 'A',
            templateUrl: '/views/templates/metaQuery.html',
            replace: true,
            transclude: true,
            scope: {
                value: '=value',
                compound: '=compound'
            },
            link: linkFunc,
            controller: gwMetaQueryController
        };

        return directive;
    }



    //TODO: Delete once app works
    function linkFunc($scope, element, attrs, ngModel) {

    }

    //controller to handle building new queries
    /* @ngInject */
    function gwMetaQueryController($scope, $element, SpectraQueryBuilderService, $location) {

        //receive a click
        $scope.newQuery = function() {
            //build a mona query based on this label
            SpectraQueryBuilderService.prepareQuery();

            //add it to query
            SpectraQueryBuilderService.addMetaDataToQuery($scope.value, $scope.compound);

            //assign to the cache

            //run the query and show it's result in the spectra browser
            $location.path("/spectra/browse/");
        };

        //receive a click
        $scope.addToQuery = function() {
            SpectraQueryBuilderService.addMetaDataToQuery($scope.value, $scope.compound);
            $location.path("/spectra/browse/");
        };


        //receive a click
        $scope.removeFromQuery = function() {
            //build a mona query based on this label
            SpectraQueryBuilderService.removeMetaDataFromQuery($scope.value, $scope.compound);

            //run the query and show it's result in the spectra browser
            $location.path("/spectra/browse/");
        };
    }
})();