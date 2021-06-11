/**
 * Created by wohlgemuth on 6/9/14.
 */

/**
 * simple service to help with available tags
 */

import * as angular from 'angular';

class MetadataService{
    private static $inject = ['REST_BACKEND_SERVER', '$http'];
    private REST_BACKEND_SERVER;
    private $http;

    constructor(REST_BACKEND_SERVER, $http) {
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
        this.$http = $http;
        this.$http.defaults.useXDomain = true;
    }

    metadata = () => {
        const config = {
            isArray: true
        };
        return this.$http.get(this.REST_BACKEND_SERVER + '/rest/meta/data?max=100', config);
    }

    categories = () => {
        const config = {
            isArray: true
        };
        return this.$http.get(this.REST_BACKEND_SERVER + '/rest/meta/category?max=100', config);
    }

    categoryData = () => {
        const config = {
            isArray: true
        };
        return this.$http.get(this.REST_BACKEND_SERVER + '/rest/meta/category/data?max=100', config);
    }

    dataValues = () => {
        const config = {
            isArray: true
        };
        return this.$http.get(this.REST_BACKEND_SERVER + '/rest/meta/data/value?max=100', config);
    }

    queryValues = (data) => {
        const config = {
            isArray: true,
            headers: {
                'Content-Type': 'application/json'
            },
            params: {query: '@query'}
        };
        return this.$http.post(this.REST_BACKEND_SERVER + '/rest/meta/data/search?max=10');
    }

    metaDataNames = () => {
        const config = {
            isArray: true,
            headers: {
                'Content-Type': 'application/json'
            },
            cache: true
        };
        return this.$http.get(this.REST_BACKEND_SERVER +'/rest/metaData/names', config);
    }
}


angular.module('moaClientApp')
    .service('MetadataService', MetadataService);

