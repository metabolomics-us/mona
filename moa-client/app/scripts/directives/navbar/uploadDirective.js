/*
 * renders upload button for nav bar
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('uploadButton', uploadButton);

    function uploadButton() {
        var directive = {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/upload.html'
        };
        return directive;
    }
})();