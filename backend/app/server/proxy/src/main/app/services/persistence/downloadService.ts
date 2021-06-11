/**
 * Created by sajjan on 10/17/2017.
 */

import * as angular from 'angular';

class DownloadService{
    private static $inject = ['REST_BACKEND_SERVER', '$http'];
    private REST_BACKEND_SERVER;
    private $http;
    constructor(REST_BACKEND_SERVER, $http) {
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
        this.$http = $http;
        this.$http.defaults.useXDomain = true;
    }

    getPredefinedQueries = () => {
        const api = this.REST_BACKEND_SERVER +'/rest/downloads/predefined';
        const config = {
            headers: {
                'Content-Type': 'application/json'
            },
            isArray: true
        }
        return this.$http.get(api, config);
    }

    getStaticDownloads = () => {
        const api = this.REST_BACKEND_SERVER +'/rest/downloads/static';
        const config = {
            headers: {
                'Content-Type': 'application/json'
            }
        }
        return this.$http.get(api, config);
    }

}

angular.module('moaClientApp')
    .service('DownloadService', DownloadService);


