/**
 * links a metadata field to a query builder and executes the spectra query for us
 */

(function() {
    'use strict';

    gwSubmitterQueryController.$inject = ['$scope', '$element', 'SpectraQueryBuilderService', '$location', 'Spectrum'];
    angular.module('moaClientApp')
      .directive('gwSubmitterQuery', gwSubmitterQuery);

    function gwSubmitterQuery() {
        var directive = {
            replace: true,
            transclude: true,
            templateUrl: '/views/templates/submitter/query.html',
            restrict: 'A',
            scope: {
                submitter: '=submitter'
            },
            link: linkFunc,
            controller: gwSubmitterQueryController


        };

        return directive;
    }

    function linkFunc($scope, element, attrs, ngModel) {

    }


    //controller to handle building new queries
    /* @ngInject */
    function gwSubmitterQueryController($scope, $element, SpectraQueryBuilderService, $location, Spectrum) {

        //receive a click
        $scope.newQuery = function() {
            //build a mona query based on this label
            SpectraQueryBuilderService.prepareQuery();

            //add it to query
            SpectraQueryBuilderService.addUserToQuery($scope.submitter.emailAddress);

            //run the query and show it's result in the spectra browser
            $location.path("/spectra/browse/");
        };

        //receive a click
        $scope.addToQuery = function() {
            SpectraQueryBuilderService.addUserToQuery($scope.submitter.emailAddress);
            $location.path("/spectra/browse/");
        };


        //receive a click
        $scope.removeFromQuery = function() {
            //build a mona query based on this label
            SpectraQueryBuilderService.removeUserFromQuery($scope.submitter.emailAddress);

            //run the query and show it's result in the spectra browser
            $location.path("/spectra/browse/");
        };

        $scope.curateSpectra = function() {
            //build a mona query based on this label
            SpectraQueryBuilderService.prepareQuery();

            //add it to query
            SpectraQueryBuilderService.addUserToQuery($scope.submitter.emailAddress);

            var query = SpectraQueryBuilderService.getQuery();

            Spectrum.curateSpectraByQuery(query, function(data) {
            });
        }
    }
})();