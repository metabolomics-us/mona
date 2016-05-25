/*
 * Component to render our Search Box
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('searchBox', searchBox);

    function searchBox() {
        var directive = {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/searchBox.html'
        };

        return directive;
    }
})();