/**
 * Created by wohlgemuth
 *
 * simple service to help with available tags
 */

import * as angular from 'angular';

class StatisticsService{
    private static $inject = ['REST_BACKEND_SERVER', '$http'];
    private REST_BACKEND_SERVER;
    private $http;
    constructor(REST_BACKEND_SERVER, $http){
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
        this.$http = $http;
    }

    executionTime = (data) => {
        const config = {
            headers: {
                'Content-Type': 'application/json'
            },
            params: {
                max: data.max
            }
        };
        let api = this.REST_BACKEND_SERVER + '/rest/statistics/category/' + data.method + '/' + data.time;
        return this.$http.get(api, config);
    }

    pendingJobs = () => {
        const config = {
                headers: {
                    'Content-Type': 'application/json'
                }
        };
        return this.$http.get(this.REST_BACKEND_SERVER + '/rest/statistics/jobs/pending', config);
    }

    spectraCount = (data) => {
        const config = {
            headers: {
                'Content-Type': 'application/json'
            }
        }
        return this.$http.get(this.REST_BACKEND_SERVER + '/rest/statistics/submitters/count/' + data.id, config);
    }

    spectraTopScores = () => {
        const config = {
            headers: {
                'Content-Type': 'application/json'
            }
        }
        return this.$http.get(this.REST_BACKEND_SERVER + '/rest/statistics/submitters', config);
    }
}

angular.module('moaClientApp')
    .service('StatisticsService', StatisticsService);
