/*
 * Component to render Header for our navbar
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('titleHeader', titleHeader);

    function titleHeader() {
        var directive = {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/titleHeader.html'
        };

        return directive;
    }
})();