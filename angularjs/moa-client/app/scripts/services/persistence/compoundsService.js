/**
 * Created by wohlgemuth on 6/12/14.
 */
'use strict';

app.factory('Compound', function ($resource, REST_BACKEND_SERVER, $http) {

    $http.defaults.useXDomain = true;

    return $resource(
            REST_BACKEND_SERVER + '/rest/compounds/:id?max=50',
        {id: "@id"},
        {
            'update': {
                method: 'PUT'

            }
        }
    );
});
