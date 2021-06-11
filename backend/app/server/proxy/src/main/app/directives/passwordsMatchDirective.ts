/**
 * Created by sajjan on 9/7/15.
 */

import * as angular from 'angular';

class PasswordsMatchDirective {
    constructor() {
        return {
            restrict: 'A',
            scope:  true,
            require:  'ngModel',
            link: (scope, elem, attrs, control) =>{
                let checker = () => {
                    return scope.$eval(attrs.ngModel) === scope.$eval(attrs.passwordMatch);
                };

                scope.$watch(checker, (x) => {
                    //set the form control to valid if both
                    //passwords are the same, else invalid
                    control.$setValidity("unique", x);
                });
            }
        }
    }
}

angular.module('moaClientApp')
    .directive('passwordMatch', PasswordsMatchDirective);
