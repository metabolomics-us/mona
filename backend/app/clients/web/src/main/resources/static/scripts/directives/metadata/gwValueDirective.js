/**
 * used to render a metadata value field
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('gwValue', gwValue);

    function gwValue() {
        var directive = {
            restrict: 'A',
            templateUrl: '/views/templates/metaValue.html',
            scope: {
                value: '=value'
            },
            link: linkFunc
        };

        return directive;
    }

    function linkFunc($scope, element, attrs, ngModel) {
        if ($scope.value.computed === true) {
            element.append("<i class='fa fa-flask'></i>");
        }
    }
})();