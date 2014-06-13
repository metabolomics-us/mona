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
            REST_BACKEND_SERVER + '/rest/tags/:id',
        {id: "@id"},
        {
            'update': {
                method: 'PUT'

            }
        }
    );
});