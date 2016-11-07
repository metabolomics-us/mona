/**
 * Executes a tag query
 */

(function() {
    'use strict';

    tagQueryController.$inject = ['$scope', 'SpectraQueryBuilderService', '$location'];
    angular.module('moaClientApp')
        .directive('tagQuery', tagQuery);

    function tagQuery() {
        return {
            restrict: 'A',
            templateUrl: '/views/templates/query/tagQuery.html',
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
    }

    /* @ngInject */
    function tagQueryController($scope, SpectraQueryBuilderService, $location) {
        /**
         * Create a new query based on the selected tag value
         */
        $scope.newQuery = function() {
            SpectraQueryBuilderService.prepareQuery();
            $scope.addToQuery();
        };

        /**
         * Add selected tag value to the current query
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