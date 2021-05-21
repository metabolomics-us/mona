/*
 * Component to render Header for our navbar
 */

import * as angular from 'angular';

    angular.module('moaClientApp')
        .directive('titleHeader', titleHeader);

    function titleHeader() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '../../views/navbar/titleHeader.html'
        };
    }
