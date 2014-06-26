/**
 * Created by wohlgemuth on 6/11/14.
 */
'use strict';

app.factory('Spectrum', function ($resource, REST_BACKEND_SERVER, $http) {

    $http.defaults.useXDomain = true;

    return $resource(
            REST_BACKEND_SERVER + '/rest/spectra/:id?max=50',
        {id: "@id"},
        {
            'update': {
                method: 'PUT'

            }
        }
    );
});
