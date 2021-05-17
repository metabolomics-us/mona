/*
 * renders download button for nav bar
 */

import * as angular from 'angular';

    angular.module('moaClientApp')
        .directive('downloadButton', downloadButton);

    function downloadButton() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/download.html'
        };
    }
