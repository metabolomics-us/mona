/**
 * Created by wohlgemuth
 *
 * simple service to help with available tags
 */

import * as angular from 'angular';

    statisticsService.$inject = ['$resource', 'REST_BACKEND_SERVER'];
    angular.module('moaClientApp')
        .factory('StatisticsService', statisticsService);

    /* @ngInject */
    function statisticsService($resource, REST_BACKEND_SERVER) {

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
                    
                },
                'spectraTopScores': {
                    url: REST_BACKEND_SERVER + '/rest/statistics/submitters',
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: true
                }
            }
        );
    }
