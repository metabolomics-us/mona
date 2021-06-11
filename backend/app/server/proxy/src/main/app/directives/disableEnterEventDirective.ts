/**
 * Created by wohlgemuth on 6/12/14.
 *
 * disables automatic form submission when you press enter in an input element
 * TODO: directive usage can't be found.
 */
import * as angular from 'angular';

class DisableEnterEventDirective {
    constructor() {
        return {
            link: (scope, element, attrs) => {
                element.bind("keydown keypress", (event) => {
                    if (event.which === 13) {
                        event.preventDefault();
                    }
                });
            }
        }
    }
}

angular.module('moaClientApp')
    .directive('flNoSubmit', DisableEnterEventDirective)
