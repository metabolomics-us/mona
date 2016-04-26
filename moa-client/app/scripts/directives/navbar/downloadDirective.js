/*
 * renders download button for nav bar
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('downloadButton', downloadButton);

    function downloadButton() {
        var directive = {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/download.html'
        };
        return directive;
    }
})();