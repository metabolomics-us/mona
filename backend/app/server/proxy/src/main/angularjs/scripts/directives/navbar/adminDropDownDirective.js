/*
 * Component to render our Admin drop down menu
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
        .directive('adminDropDown', adminDropDown);

    function adminDropDown() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/adminDropdown.html'
        };
    }
})();