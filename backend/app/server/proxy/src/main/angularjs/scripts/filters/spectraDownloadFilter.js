/**
 * Created by wohlgemuth on 11/7/14.
 */


(function() {
    'use strict';

    /**
     * generates a curl link for us
     */
    angular.module('moaClientApp')
      .filter('spectraDownload', ['REST_BACKEND_SERVER', function(REST_BACKEND_SERVER) {
          return function(input) {
              return REST_BACKEND_SERVER + '/rest/spectra/' + input + "?format=mona";
          };
      }])

    /**
     * generates a curl link as msp file for us
     */
      .filter('spectraDownloadAsMsp', ['REST_BACKEND_SERVER', function(REST_BACKEND_SERVER) {
          return function(input) {
              return REST_BACKEND_SERVER + '/rest/spectra/' + input + "?format=msp";
          };
      }])

    /**
     * as mol file
     */
      .filter('compoundDownloadAsMolFile', ['REST_BACKEND_SERVER', function(REST_BACKEND_SERVER) {
          return function(input) {
              return REST_BACKEND_SERVER + '/rest/compounds/' + input + "?format=mol";
          };
      }])

    /**
     * as mol file
     */
      .filter('compoundDownloadAsSDFFile', ['REST_BACKEND_SERVER', function(REST_BACKEND_SERVER) {
          return function(input) {
              return REST_BACKEND_SERVER + '/rest/compounds/' + input + "?format=sdf";
          };
      }])

    /**
     * as mona record file
     */
      .filter('compoundDownloadFile', ['REST_BACKEND_SERVER', function(REST_BACKEND_SERVER) {
          return function(input) {
              return REST_BACKEND_SERVER + '/rest/compounds/' + input + "?format=mona";
          };
      }]);
})();