/**
 * Created by wohlgemuth on 6/11/14.
 */
'use strict';

app.factory('Spectrum', function ($resource, REST_BACKEND_SERVER, $http) {

    //$http.defaults.useXDomain = true;

    return $resource(
            REST_BACKEND_SERVER + '/rest/spectra/:id',
        {id: "@id"},
        {
            /**
             * update matehod
             */
            'update': {
                method: 'PUT'

            },

            /**
             * connects to our service and executes a query
             */
            'searchSpectra': {
                url: REST_BACKEND_SERVER + '/rest/spectra/search',
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: true
            }
        }
    );
});
