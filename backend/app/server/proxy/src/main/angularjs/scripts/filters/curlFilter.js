/**
 * Created by wohlgemuth on 11/6/14.
 */


(function() {
    'use strict';

    /**
     * generates a curl link for us
     */
    angular.module('moaClientApp')
      .filter('curl', ['REST_BACKEND_SERVER', function(REST_BACKEND_SERVER) {
          return function(input) {
              // TODO Receiving empty spectrum objects?
              console.log(input);

              input = input && typeof(input) == 'string' ? input.replace(/"/g, '\\"') : '';
              return 'curl "'+ REST_BACKEND_SERVER +'/rest/spectra/search?query='+ input +'"';
          };
      }])

    /**
     * generates a curl link as msp file for us
     */
      .filter('curlAsMsp', ['curlFilter', function(curlFilter) {
          return function(input) {
              var object = angular.copy(input);
              object.format = "msp";
              return curlFilter(object)
          };
      }]);
})();
