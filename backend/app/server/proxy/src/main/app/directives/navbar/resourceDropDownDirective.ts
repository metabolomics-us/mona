/*
 * Component to render our Resources drop down menu
 */

import * as angular from 'angular';

class ResourceDropDownDirective {
    constructor() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '../../views/navbar/resDropdown.html'
        };
    }
}

angular.module('moaClientApp')
    .directive('resourceDropDown', ResourceDropDownDirective);

