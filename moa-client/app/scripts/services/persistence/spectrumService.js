/**
 * Created by wohlgemuth on 6/11/14.
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .factory('Spectrum', Spectrum);

    /* @ngInject */
    function Spectrum($resource, REST_BACKEND_SERVER, MAX_SPECTRA) {

        /**
         * creates a new resources, we can work with
         */
        return $resource(
          REST_BACKEND_SERVER + '/rest/spectra/:id?max=' + MAX_SPECTRA + ':offset',
          {id: "@id", offset: "@offset"},
          {
              /**
               * update method
               */
              'update': {
                  method: 'PUT'
              },

              /**
               * connects to our service and executes a query
               */
              'searchSpectra': {
                  url: REST_BACKEND_SERVER + '/rest/spectra/search?max=' + MAX_SPECTRA,
                  method: 'POST',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  isArray: true
              },

              'searchSpectraCount': {
                  url: REST_BACKEND_SERVER + '/rest/spectra/searchCount',
                  method: 'POST',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  isArray: false
              },

              /**
               * searches for similar spectra
               */
              'searchSimilarSpectra': {
                  url: REST_BACKEND_SERVER + '/rest/spectra/similarity?max=' + MAX_SPECTRA,
                  method: 'POST',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  isArray: false
              },

              /**
               * sends the object to the server to be processed and executed at their convenience. Meaning no intermediate feedback is provided or required.
               */
              'batchSave': {
                  url: REST_BACKEND_SERVER + '/rest/spectra/batch/save',
                  method: 'POST',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  isArray: false
              },

              /**
               * sends a request to the server to score this entity
               */
              'score': {
                  url: REST_BACKEND_SERVER + '/rest/spectra/score/:id/explain',
                  method: 'GET',
                  headers: {
                      'Content-Type': 'application/json'
                  },
                  isArray: false
              },

              /**
               * sends a request to the server to curate this spectrum
               */
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