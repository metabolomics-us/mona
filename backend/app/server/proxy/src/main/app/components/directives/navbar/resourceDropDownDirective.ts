/*
 * Component to render our Resources drop down menu
 */

import * as angular from 'angular';

    angular.module('moaClientApp')
        .directive('resourceDropDown', resDropDown);

    function resDropDown() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/resDropdown.html'
        };
    }
