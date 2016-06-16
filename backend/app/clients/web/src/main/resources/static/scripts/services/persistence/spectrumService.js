/**
 * Created by wohlgemuth on 6/11/14.
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .factory('Spectrum', spectrum);

    /* @ngInject */
    function spectrum($resource, REST_BACKEND_SERVER, MAX_SPECTRA) {

        /**
         * creates a new resources, we can work with
         */

        return $resource(
            REST_BACKEND_SERVER + '/rest/spectra/:id',
            {id: "@id", offset: "@offset"},
            {
                'update': {
                    method: 'PUT'
                },
                'searchSpectra': {
                    url: REST_BACKEND_SERVER + '/rest/spectra/:endpoint',
                    method: 'GET',
                    isArray: true
                },
                'searchSpectraCount': {
                    url: REST_BACKEND_SERVER + '/rest/spectra/search/:endpoint',
                    method: 'GET',
                    isArray: false,
                    transformResponse: function(data) {
                        return {count: data};
                    }
                },
                'searchSimilarSpectra': {
                    url: REST_BACKEND_SERVER + '/rest/spectra/similarity?max=' + MAX_SPECTRA,
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'batchSave': {
                    url: REST_BACKEND_SERVER + '/rest/spectra/batch/save',
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'score': {
                    url: REST_BACKEND_SERVER + '/rest/spectra/score/:id/explain',
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'curate': {
                    url: REST_BACKEND_SERVER + '/rest/spectra/curate/:id',
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'curateSpectraByQuery': {
                    url: REST_BACKEND_SERVER + '/rest/curate/spectra/curateAllByQuery',
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'associateSpectraByQuery': {
                    url: REST_BACKEND_SERVER + '/rest/spectra/associate/allByQuery',
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'getPredefinedQueries': {
                    url: REST_BACKEND_SERVER + '/rest/stored/query?max=100',
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