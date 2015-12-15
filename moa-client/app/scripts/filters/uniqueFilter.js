/**
 * used in displayCompound.html to filter out duplicate compounds
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .filter('unique', function() {
          return function(input, key) {
              var unique = {};
              var uniqueList = [];
              if (input != null) {
                  for (var i = 0; i < input.length; i++) {
                      if (typeof unique[input[i][key]] == "undefined") {
                          unique[input[i][key]] = "";
                          uniqueList.push(input[i]);
                      }
                  }
              }
              return uniqueList;
          };
      });
})();