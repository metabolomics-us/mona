/**
 * Created by wohlgemuth on 6/9/14.
 * simple service to help with available tags
 */

(function() {
    'use strict';
    taggingService.$inject = ['$resource', 'REST_BACKEND_SERVER', '$http'];
    angular.module('moaClientApp')
        .factory('TaggingService', taggingService);

    /* @ngInject */
    function taggingService($resource, REST_BACKEND_SERVER, $http) {

        $http.defaults.useXDomain = true;

        return $resource(
            REST_BACKEND_SERVER +'/rest/tags', {},
            {
                'library': {
                    url: REST_BACKEND_SERVER + '/rest/tags/library',
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: true
                }
            }
        );
    }
})();
