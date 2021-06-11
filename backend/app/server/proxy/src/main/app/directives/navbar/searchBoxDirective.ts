/*
 * Component to render our Search Box
 */

import * as angular from 'angular';

class SearchBoxDirective {
    constructor() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '../../views/navbar/searchBox.html'
        };
    }
}

angular.module('moaClientApp')
    .directive('searchBox', SearchBoxDirective);
