/*
 * Component to render our Admin drop down menu
 */

import * as angular from 'angular';

    angular.module('moaClientApp')
        .directive('adminDropDown', adminDropDown);

    function adminDropDown() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '../../views/navbar/adminDropdown.html'
        };
    }
