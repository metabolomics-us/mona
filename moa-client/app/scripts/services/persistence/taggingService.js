/**
 * Created by wohlgemuth on 6/9/14.
 */

'use strict';

/**
 * simple service to help with available tags
 */

app.factory('TaggingService', function ($resource, REST_BACKEND_SERVER, $http) {

    $http.defaults.useXDomain = true;

    return $resource(
            REST_BACKEND_SERVER + '/rest/tags/:id?max=100',
        {id: "@id"},
        {
            'update': {
                method: 'PUT'

            },
            'statistics': {
                url: REST_BACKEND_SERVER + '/rest/statistics/tags/spectra/countAll?max='+ 100,
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: true
            }
        }
    );
});