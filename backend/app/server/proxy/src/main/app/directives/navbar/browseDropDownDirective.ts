/*
 * Component to render our Browse drop down menu
 */

import * as angular from 'angular';

class BrowseDropDownDirective {
    constructor() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '../../views/navbar/browseDropdown.html',
            controller: BrowseDropDownController,
            controllerAs: '$ctrl'
        };
    }
}

class BrowseDropDownController {
    private static $inject = ['$scope', 'SpectraQueryBuilderService'];
    private $scope;
    private SpectraQueryBuilderService;

    constructor($scope, SpectraQueryBuilderService) {
        this.$scope = $scope;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
    }

    // Reset query when user click browse
    resetQuery = () => {
        this.SpectraQueryBuilderService.prepareQuery();
    }
}

angular.module('moaClientApp')
    .directive('browseDropDown', BrowseDropDownDirective);
