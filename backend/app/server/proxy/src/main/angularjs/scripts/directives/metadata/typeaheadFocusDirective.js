/**
 * simple directive to help populating the type ahead views on focis
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
        .directive('typeaheadFocus', typeaheadFocus);

    function typeaheadFocus() {
        return {
            require: 'ngModel',
            link: linkFunc
        };
    }

    function linkFunc(scope, element, attr, ngModel) {

        //trigger the popup on 'click' because 'focus'
        //is also triggered after the item selection
        element.bind('click', function() {

            var viewValue = ngModel.$viewValue;

            //restore to null value so that the typeahead can detect a change
            if (ngModel.$viewValue === ' ') {
                ngModel.$setViewValue(null);
            }

            //force trigger the popup
            ngModel.$setViewValue(' ');

            //set the actual value in case there was already a value in the input
            ngModel.$setViewValue(viewValue || ' ');
        });

        //compare function that treats the empty space as a match
        scope.emptyOrMatch = function(actual, expected) {
            if (expected === ' ') {
                return true;
            }
            return actual.indexOf(expected) > -1;
        };
    }
})();
