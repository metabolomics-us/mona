/**
 * Created by wohlgemuth on 6/11/14.
 */
'use strict';

app.factory('Spectrum', function ($resource, REST_BACKEND_SERVER, MAX_OBJECTS, $http) {

    //$http.defaults.useXDomain = true;

    return $resource(
            REST_BACKEND_SERVER + '/rest/spectra/:id?max='+ MAX_OBJECTS +':offset',
        {id: "@id", offset: "@offset"},
        {
            'update': {
                method: 'PUT'

            }
        }
    );
});
