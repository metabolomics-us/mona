/*
 * Component to render Header for our navbar
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
        .directive('titleHeader', titleHeader);

    function titleHeader() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/titleHeader.html'
        };
    }
})();