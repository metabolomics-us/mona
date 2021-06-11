/*
 * Component to render Header for our navbar
 */

import * as angular from 'angular';

class TitleHeaderDirective {
    constructor() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '../../views/navbar/titleHeader.html'
        };
    }
}

angular.module('moaClientApp')
    .directive('titleHeader', TitleHeaderDirective);
