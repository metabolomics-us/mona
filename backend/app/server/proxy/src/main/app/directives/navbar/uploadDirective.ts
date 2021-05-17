/*
 * renders upload button for nav bar
 */

import * as angular from 'angular';

    angular.module('moaClientApp')
        .directive('uploadButton', uploadButton);

    function uploadButton() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/upload.html'
        };
    }
