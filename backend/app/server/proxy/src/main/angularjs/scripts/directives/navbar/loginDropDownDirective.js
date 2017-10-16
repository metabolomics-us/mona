/*
 * Component to render our Login menu
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
        .directive('loginDropDown', loginDropDown);

    function loginDropDown() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/loginDropdown.html'
        };
    }
})();