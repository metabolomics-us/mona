/**
 * Created by wohlgemuth on 6/9/14.
 */

/**
 * simple service to help with available tags
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .factory('MetadataService', MetadataService);

    /* @ngInject */
    function MetadataService($resource, REST_BACKEND_SERVER, $http) {
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
              metadata: {
                  method: "GET",
                  isArray: true,
                  params: {
                      controller: "data"
                  }
              },
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
              },
              queryValues: {
                  url: REST_BACKEND_SERVER + '/rest/meta/data/search?max=10',
                  method: 'POST',
                  isArray: true,
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  params: {query: '@query'}
              }
          }
        );
    }
})();


