/**
 * Created by wohlgemuth on 6/11/14.
 */
'use strict';

app.factory('Spectrum', function ($resource, REST_BACKEND_SERVER, MAX_OBJECTS) {

    /**
     * creates a new resources, we can work with
     */
    return $resource(
        REST_BACKEND_SERVER + '/rest/spectra/:id?max=' + MAX_OBJECTS + ':offset',
        {id: "@id", offset: "@offset"},
        {
            /**
             * update method
             */
            'update': {
                method: 'PUT'

            },

            /**
             * connects to our service and executes a query
             */
            'searchSpectra': {
                url: REST_BACKEND_SERVER + '/rest/spectra/search?max=' + MAX_OBJECTS,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: true
            },


            /**
             * searches for similar spectra
             */
            'searchSimilarSpectra': {
                url: REST_BACKEND_SERVER + '/rest/spectra/similarity',
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: false
            },

            /**
             * sends the object to the server to be processed and executed at their convenience. Meaning no intermediate feedback is provided or required.
             */
            'batchSave': {
                url: REST_BACKEND_SERVER + '/rest/spectra/batch/save',
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: false
            },
            /**
             * sends a request to the server to score this entity
             */
            'score': {
                url: REST_BACKEND_SERVER + '/rest/spectra/score/:id/explain',
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: false
            },
            /**
             * sends a request to the server to curate this spectrum
             */
            'curate': {

                url: REST_BACKEND_SERVER + '/rest/spectra/curate/:id',
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: false
            }


        }
    );
});
