/**
 * Created by wohlgemuth
 *
 * simple service to help with available tags
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .factory('StatisticsService', StatisticsService);

    /* @ngInject */
    function StatisticsService($resource, REST_BACKEND_SERVER, $http) {

        return $resource(
          REST_BACKEND_SERVER + '/rest/statistics/:time',
          {time: "@time", method: "@method", max: "@max", id: "@id"},
          {
              'executionTime': {
                  url: REST_BACKEND_SERVER + '/rest/statistics/category/:method/:time?max=:max',
                  method: 'GET',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  isArray: true
              },
              'pendingJobs': {
                  url: REST_BACKEND_SERVER + '/rest/statistics/jobs/pending',
                  method: 'GET',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  isArray: true
              },
              'spectraCount': {
                  url: REST_BACKEND_SERVER + '/rest/statistics/submitters/count/:id',
                  method: 'GET',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  isArray: false
              }
              ,
              'spectraScore': {
                  url: REST_BACKEND_SERVER + '/rest/statistics/submitters/score/:id',
                  method: 'GET',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  isArray: false
              },
              'spectraTopScores': {
                  url: REST_BACKEND_SERVER + '/rest/statistics/submitters/scores?max=:max',
                  method: 'GET',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  isArray: true
              }
          }
        );
    }
})();