/**
 * Executes a tag query
 */

(function() {
    'use strict';

    tagQueryController.$inject = ['$scope', 'SpectraQueryBuilderService', '$location'];
    angular.module('moaClientApp')
        .directive('tagQuery', tagQuery);

    function tagQuery() {
        var directive = {
            restrict: 'A',
            templateUrl: '/views/templates/tagQuery.html',
            replace: true,
            scope: {
                ruleBased: '=',
                type: '@',
                tag: '=value',
                size: '@'
            },
            priority: 1001,
            controller: tagQueryController
        };

        return directive;
    }

    /* @ngInject */
    function tagQueryController($scope, SpectraQueryBuilderService, $location) {
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
            if (angular.isDefined($scope.type) && $scope.type == 'compound') {
                SpectraQueryBuilderService.addCompoundTagToQuery($scope.tag.text);
            } else {
                SpectraQueryBuilderService.addTagToQuery($scope.tag.text);
            }

            SpectraQueryBuilderService.executeQuery();
        };
    }
})();