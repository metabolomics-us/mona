/**
 * Creates or updates a query based on SPLASH
 */

import * as angular from 'angular';

class SplashQueryDirective {
    constructor() {
        return {
            restrict: 'A',
            templateUrl: '../../views/templates/query/splashQuery.html',
            replace: true,
            transclude: true,
            scope: {
                value: '=value'
            },
            controller: SplashQueryController,
            controllerAs: '$ctrl'
        };
    }
}

class SplashQueryController {
    private static $inject = ['$scope', 'SpectraQueryBuilderService'];
    private $scope;
    private SpectraQueryBuilderService;
    constructor($scope, SpectraQueryBuilderService) {
        this.$scope = $scope;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
    }

    $onInit = () => {
        console.log(this.$scope.value.splash);
        console.log(this.$scope.value);
    }

    /**
     * Create a new query based on the selected SPLASH
     */
    newQuery = () => {
        console.log("WHAT THE FUCK IS GOIGN ON");
        this.SpectraQueryBuilderService.prepareQuery();
        this.addToQuery();
    };

    /**
     * Add selected SPLASH to the current query
     */
    addToQuery = () => {
        console.log("WHAT THE FUCK IS GOIGN ON");
        this.SpectraQueryBuilderService.addSplashToQuery(this.$scope.value.splash);
        this.SpectraQueryBuilderService.executeQuery();
    };
}

angular.module('moaClientApp')
    .directive('splashQuery', SplashQueryDirective);

