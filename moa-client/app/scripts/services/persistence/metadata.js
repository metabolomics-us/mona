(function() {
    'use strict';
    angular.module('moaClientApp')
        .factory('MetaData', metaData);

    /* @ngInject */
    function metaData($resource, REST_BACKEND_SERVER, $http) {
        $http.defaults.useXDomain = true;

        return $resource(
            REST_BACKEND_SERVER + '/rest/meta/data/:id', {id: "@id"}, {
                'update': {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }
            }
        );
    }
})();
