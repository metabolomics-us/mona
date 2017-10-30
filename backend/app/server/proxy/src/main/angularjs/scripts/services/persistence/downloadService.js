/**
 * Created by sajjan on 10/17/2017.
 */

(function() {
    'use strict';

    downloadService.$inject = ['$resource', 'REST_BACKEND_SERVER', '$http'];
    angular.module('moaClientApp')
        .factory('DownloadService', downloadService);

    /* @ngInject */
    function downloadService($resource, REST_BACKEND_SERVER, $http) {

        $http.defaults.useXDomain = true;

        return $resource(
            REST_BACKEND_SERVER +'/rest', {},
            {
                'getPredefinedQueries': {
                    url: REST_BACKEND_SERVER +'/rest/downloads/predefined',
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: true
                },
                'getStaticDownloads': {
                    url: REST_BACKEND_SERVER +'/rest/downloads/static',
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
