/**
 * Created by wohlgemuth on 6/11/14.
 */
'use strict';

app.factory('Spectrum', function ($resource, REST_BACKEND_SERVER, MAX_OBJECTS) {

    /**
     * creates a new resources, we can work with
     */
    return $resource(
            REST_BACKEND_SERVER + '/rest/spectra/:id?max='+ MAX_OBJECTS +':offset',
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
                url: REST_BACKEND_SERVER + '/rest/spectra/search?max='+ MAX_OBJECTS,
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: true
            },

            /**
             * sends the object to the server to be processed and executed at their convinience. Meaning no intermidiate feedback is provided or required.
             */
            'batchSave': {
                url: REST_BACKEND_SERVER + '/rest/spectra/batch/save',
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: false
            }


        }
    );
});
