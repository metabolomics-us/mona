/**
 * Created by wohlgemuth on 6/12/14.
 */
'use strict';

app.factory('Compound', function ($resource, REST_BACKEND_SERVER, MAX_COMPOUNDS) {
    return $resource(REST_BACKEND_SERVER + '/rest/compounds/:id?max='+ MAX_COMPOUNDS,
        {id: "@id", offset: "@offset"},
        {
            'update': {
                method: 'PUT'
            }
        }
    );
});
