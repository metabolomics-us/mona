/**
 * Created by Gert on 5/28/2014.
 */
import * as angular from 'angular';

class SubmitterService{
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

    update = (id) => {
        return this.$http.put(this.REST_BACKEND_SERVER + '/rest/submitters/' + id);
    }
}

angular.module('moaClientApp')
    .service('Submitter', SubmitterService);

