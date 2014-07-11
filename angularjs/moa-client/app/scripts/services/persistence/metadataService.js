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
        REST_BACKEND_SERVER + '/rest/meta/:controller/:id/:subController/:subID/:subSubController?max=100',
        {
            controller: "@controller",
            id: "@id",
            categoryController: "@subController",
            dataID: "@subID",
            dataController: "@subSubController"
        },
        {
            categories: {
                method: "GET",
                isArray: true,
                params: {
                    controller: "category"
                }
            },
            categoryData: {
                method: "GET",
                isArray: true,
                params: {
                    controller: "category",
                    subController: "data"
                }
            },
            dataValues: {
                method: "GET",
                isArray: true,
                params: {
                    controller: "data",
                    subController: "value"
                }
            }
        }
    );
});