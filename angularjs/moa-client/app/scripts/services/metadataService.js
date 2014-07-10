/**
 * Created by wohlgemuth on 6/9/14.
 */

'use strict';

/**
 * simple service to help with available tags
 */

app.factory('MetadataService', function ($resource, REST_BACKEND_SERVER, $http) {
    $http.defaults.useXDomain = true;

    return $resource(
        REST_BACKEND_SERVER + '/rest/meta/category/:id/:categoryController/:dataID/:dataController',
        {
            id: "@id",
            categoryController: "@categoryController",
            dataID: "@dataID",
            dataController: "@dataController"
        }
        /*,
        {
            names: {
                method: "GET",
                params: {
                    categoryController: "data"
                }
            },
            spectra: {
                method: "GET",
                params: {
                    categoryController: "data",
                    dataController: "spectra"
                }

            }
        }
        */
    );
});