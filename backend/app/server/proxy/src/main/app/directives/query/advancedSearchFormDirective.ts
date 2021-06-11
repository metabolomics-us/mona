import * as angular from 'angular';

class AdvancedSearchFormDirective {
    constructor() {
        return {
            restrict: 'E',
            templateUrl: '../../views/spectra/query/advancedSearchForm.html',
            link: (scope, elem, attrs) => {}
        };
    }
}

angular.module('moaClientApp')
    .directive('advancedSearchForm', AdvancedSearchFormDirective);
