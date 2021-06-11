/*
 * Component to render our Login menu
 */

import * as angular from 'angular';

class LoginDropDownDirective {
    constructor() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '../../views/navbar/loginDropdown.html'
        };
    }
}

angular.module('moaClientApp')
    .directive('loginDropDown', LoginDropDownDirective);
