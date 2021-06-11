/**
 * Created by wohlgemuth on 6/9/14.
 * simple service to help with available tags
 */

import * as angular from 'angular';

class TagService{
    private static $inject = ['REST_BACKEND_SERVER', '$http'];
    private REST_BACKEND_SERVER;
    private $http;

    constructor(REST_BACKEND_SERVER, $http) {
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
        this.$http = $http;
        this.$http.defaults.useXDomain = true;
    }

    query = () => {
        const config = {
            headers: {
                'Content-Type': 'application/json'
            }
        }
        return this.$http.get(this.REST_BACKEND_SERVER + '/rest/tags/library', config);
    }
}

angular.module('moaClientApp')
    .service('TagService', TagService);
