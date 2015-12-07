/**
 * Created by Gert on 5/28/2014.
 */
(function() {
    'use strict';
    angular.module('moaClientApp')
      .factory('Submitter', ['$resource', 'REST_BACKEND_SERVER', '$http',
          function($resource, REST_BACKEND_SERVER, $http) {

              $http.defaults.useXDomain = true;

              return $resource(
                REST_BACKEND_SERVER + '/rest/submitters/:id',
                {id: "@id"},
                {
                    'update': {
                        method: 'PUT'

                    }
                }
              );
          }]);
})();