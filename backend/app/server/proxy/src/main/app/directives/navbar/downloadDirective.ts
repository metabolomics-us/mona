/*
 * renders download button for nav bar
 */

import * as angular from 'angular';

class DownloadDirective {
    constructor() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '../../views/navbar/download.html'
        };
    }
}

angular.module('moaClientApp')
    .directive('downloadButton', DownloadDirective);
