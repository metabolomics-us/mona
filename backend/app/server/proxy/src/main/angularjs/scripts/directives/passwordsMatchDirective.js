/**
 * Created by sajjan on 9/7/15.
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
        .directive('passwordMatch', passwordMatch);

    function passwordMatch() {
        return {
            restrict: 'A',
            scope: true,
            require: 'ngModel',
            link: linkFunc
        };
    }

    function linkFunc(scope, elem, attrs, control) {
        var checker = function() {
            return scope.$eval(attrs.ngModel) === scope.$eval(attrs.passwordMatch);
        };

        scope.$watch(checker, function(x) {
            //set the form control to valid if both
            //passwords are the same, else invalid
            control.$setValidity("unique", x);
        });
    }
})();