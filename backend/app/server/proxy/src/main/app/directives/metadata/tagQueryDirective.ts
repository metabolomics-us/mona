/**
 * Executes a tag query
 */

import * as angular from 'angular';

class TagQueryDirective {
    constructor() {
        return {
            restrict: 'A',
            templateUrl: '../../views/templates/query/tagQuery.html',
            replace: true,
            scope: {
                ruleBased: '=',
                type: '@',
                tag: '=value',
                size: '@'
            },
            priority: 1001,
            controller: TagQueryController,
            controllerAs: '$ctrl'
        };
    }
}

class TagQueryController {
    private static $inject = ['$scope', 'SpectraQueryBuilderService', '$location'];
    private $scope;
    private SpectraQueryBuilderService;
    private $location;

    constructor($scope, SpectraQueryBuilderService, $location) {
        this.$scope = $scope;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
        this.$location = $location;
    }

    /**
     * Create a new query based on the selected tag value
     */
    newQuery = () => {
        this.SpectraQueryBuilderService.prepareQuery();
        this.addToQuery();
    };

    /**
     * Add selected tag value to the current query
     */
    addToQuery = () => {
        if (angular.isDefined(this.$scope.type) && this.$scope.type == 'compound') {
            this.SpectraQueryBuilderService.addCompoundTagToQuery(this.$scope.tag.text);
        } else {
            this.SpectraQueryBuilderService.addTagToQuery(this.$scope.tag.text);
        }

        this.SpectraQueryBuilderService.executeQuery();
    };

}

angular.module('moaClientApp')
    .directive('tagQuery', TagQueryDirective);
