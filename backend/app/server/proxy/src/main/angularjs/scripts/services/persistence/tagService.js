/**
 * Created by wohlgemuth on 6/9/14.
 * simple service to help with available tags
 */

(function() {
    'use strict';
    tagService.$inject = ['$resource', 'REST_BACKEND_SERVER', '$http'];
    angular.module('moaClientApp')
        .factory('TagService', tagService);

    /* @ngInject */
    function tagService($resource, REST_BACKEND_SERVER, $http) {

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
