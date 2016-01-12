/*
 * Component to render our Login menu
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('loginDropDown', loginDropDown);

    function loginDropDown() {
        var directive = {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/loginDropdown.html'
        };

        return directive;
    }
})();