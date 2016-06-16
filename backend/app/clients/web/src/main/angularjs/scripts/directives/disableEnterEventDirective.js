/**
 * Created by wohlgemuth on 6/12/14.
 *
 * disables automatic form submission when you press enter in an input element
 * TODO: directive usage can't be found.
 */
(function() {
    'use strict';
    angular.module('moaClientApp')
      .directive('flNoSubmit', function() {
          return function(scope, element, attrs) {
              element.bind("keydown keypress", function(event) {
                  if (event.which === 13) {
                      event.preventDefault();
                  }
              });
          };
      });
})();