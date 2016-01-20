/**
 * Created by wohlgemuth on 11/6/14.
 */


(function() {
    'use strict';

    /**
     * generates a curl link for us
     */
    angular.module('moaClientApp')
      .filter('curl', function(REST_BACKEND_SERVER) {
          return function(input) {
              return 'curl -H "Content-Type: application/json" -d \'' + angular.toJson(input, false) + '\' ' + REST_BACKEND_SERVER + '/rest/spectra/search';
          };
      })

    /**
     * generates a curl link as msp file for us
     */
      .filter('curlAsMsp', function(curlFilter) {
          return function(input) {
              var object = angular.copy(input);
              object.format = "msp";
              return curlFilter(object)
          };
      });
})();