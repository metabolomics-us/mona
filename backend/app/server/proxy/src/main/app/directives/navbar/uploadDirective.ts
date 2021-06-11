/*
 * renders upload button for nav bar
 */

import * as angular from 'angular';

class UploadDirective {
    constructor() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '../../views/navbar/upload.html'
        };
    }
}

angular.module('moaClientApp')
    .directive('uploadButton', UploadDirective);

