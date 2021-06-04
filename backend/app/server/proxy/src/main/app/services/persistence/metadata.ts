import * as angular from 'angular';

class MetadataService {
    private static $inject = ['REST_BACKEND_SERVER', '$http'];
    private REST_BACKEND_SERVER;
    private $http;

    constructor(REST_BACKEND_SERVER, $http) {
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
        this.$http = $http;
    }

    $onInit = () => {
        this.$http.defaults.useXDomain = true;
    }

    update = (data: any) => {
        const config = {
            headers: {
                'Content-Type': 'application/json'
            }
        }
        return this.$http.put(this.REST_BACKEND_SERVER + '/rest/meta/data' + data.id, data, config);
    }
}

angular.module('moaClientApp')
    .service('MetaData', MetadataService);
