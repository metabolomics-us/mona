/*
 * Component to render our Login menu
 */

import * as angular from 'angular';

    angular.module('moaClientApp')
        .directive('loginDropDown', loginDropDown);

    function loginDropDown() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '../../views/navbar/loginDropdown.html'
        };
    }
