/*
 * Component to render our Search Box
 */

import * as angular from 'angular';

    angular.module('moaClientApp')
        .directive('searchBox', searchBox);

    function searchBox() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/searchBox.html'
        };
    }
