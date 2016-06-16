/*
 * Component to render our Resources drop down menu
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('resDropDown', resDropDown);

    function resDropDown() {
        var directive = {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/resDropdown.html'
        };

        return directive;
    }
})();