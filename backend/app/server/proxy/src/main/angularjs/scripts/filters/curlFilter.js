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

              var query = input && typeof(input.query) == 'string' ? input.query.replace(/"/g, '\\"') : '';
              var text = input && typeof(input.text) == 'string' ? input.text : '';

              if (query !== '' || text !== '') {
                  var cmd = 'curl "' + host + '/rest/spectra/search?';

                  if (query !== '') {
                      cmd += 'query='+ query;

                      if (text !== '') {
                          cmd += '&text='+ text;
                      }
                  } else {
                      cmd += 'text='+ text;
                  }

                  return cmd;
              } else {
                  return 'curl "' + host + '/rest/spectra"';
              }
          };
      }])
})();
