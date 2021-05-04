import * as angular from 'angular';

    angular.module('moaClientApp')
        .directive('advancedSearchForm', advancedSearchForm);

    function advancedSearchForm() {
        return {
            restrict: 'E',
            templateUrl: 'views/spectra/query/advancedSearchForm.html',
            link: linkFunc
        };
    }

    function linkFunc(scope, elem, attrs) {
        //TODO: update DOM on search result data
    }
