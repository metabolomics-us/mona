/**
 * Created by wohlgemuth on 11/6/14.
 */


(function() {
    'use strict';

    /**
     * Generates a cURL link for us to download a given RSQL query
     */
    angular.module('moaClientApp')
      .filter('curl', ['REST_BACKEND_SERVER', function(REST_BACKEND_SERVER) {
          return function(input) {
              // Use location.host if the server url is empty for deployment mode
              var host = REST_BACKEND_SERVER == '' ? location.origin : REST_BACKEND_SERVER;

              input = input && typeof(input) == 'string' ? input.replace(/"/g, '\\"') : '';

              if (input != '') {
                  return 'curl "' + host + '/rest/spectra/search?query=' + input + '"';
              } else {
                  return 'curl "' + host + '/rest/spectra"';
              }
          };
      }])
})();
