
/*
 * Component to render our Statistics drop down menu
 */

(function() {
    'use strict';

   angular.module('moaClientApp')
     .directive('statDropDown', statDropDown);

    function statDropDown() {
        var directive = {
            restrict: 'E',
            replace: true,
            templateUrl: '/views/navbar/statsDropdown.html'
        };

        return directive;
    }
})();

