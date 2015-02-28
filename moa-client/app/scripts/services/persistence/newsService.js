'use strict';

app.factory('News', function ($resource, REST_BACKEND_SERVER, MAX_OBJECTS) {

    /**
     * creates a new resources, we can work with
     */
    return $resource(
        REST_BACKEND_SERVER + '/rest/news/:id?max='+ MAX_OBJECTS +':offset',
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
            'listAnnouncements': {
                url: REST_BACKEND_SERVER + '/rest/news/query/announcements?max='+ MAX_OBJECTS,
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: true
            },
            'listUpdates': {
                url: REST_BACKEND_SERVER + '/rest/news/query/uploads?max='+ MAX_OBJECTS,
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: true
            },
            'listNotifications': {
                url: REST_BACKEND_SERVER + '/rest/news/query/notifications?max='+ MAX_OBJECTS,
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: true
            }


        }
    );
});
