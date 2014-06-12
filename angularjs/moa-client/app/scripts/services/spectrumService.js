/**
 * Created by wohlgemuth on 6/11/14.
 */
'use strict';

app.factory('Spectrum', function ($resource, REST_BACKEND_SERVER, $http) {

    $http.defaults.useXDomain = true;

    return $resource(
            REST_BACKEND_SERVER + '/rest/spectrum/:id',
        {id: "@id"},
        {
            'update': {
                method: 'PUT'

            }
        }
    );
});
