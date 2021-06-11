/*
 * Component to render our Admin drop down menu
 */

import * as angular from 'angular';

class AdminDropDownDirective {
    constructor() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '../../views/navbar/adminDropdown.html'
        };
    }
}

angular.module('moaClientApp')
    .directive('adminDropDown', AdminDropDownDirective);
