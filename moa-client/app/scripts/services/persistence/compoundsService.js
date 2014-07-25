/**
 * Created by wohlgemuth on 6/12/14.
 */
'use strict';

app.factory('Compound', function ($resource, REST_BACKEND_SERVER, MAX_OBJECTS, $http) {

    //$http.defaults.useXDomain = true;

    return $resource(
            REST_BACKEND_SERVER + '/rest/compounds/:id?max='+ MAX_OBJECTS +':offset',
        {id: "@id", offset: "@offset"},
        {
            'update': {
                method: 'PUT'
            }
        }
    );
});
