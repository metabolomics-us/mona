/**
 * Created by sajjan on 11/6/14.
 */

(function() {
    'use strict';
    angular.module('moaClientApp')
      .controller('SpectraDatabaseIndexController', SpectraDatabaseIndexController);

    SpectraDatabaseIndexController.$inject = ['$scope', '$http', '$location', 'SpectraQueryBuilderService', 'MetadataService', 'REST_BACKEND_SERVER'];

    function SpectraDatabaseIndexController($scope, $http, $location, SpectraQueryBuilderService, MetadataService, REST_BACKEND_SERVER) {
        /**
         * Metadata that we wish to display
         * @type {string[]}
         */
        $scope.fields = [
            'instrument type',
            'ms type',
            'ion mode',
            'derivative type',
            'precursor type'
        ];

        /**
         * Loaded unique metadata values
         * @type {{}}
         */
        $scope.fieldData = {};

        /**
         *
         * @type {{}}
         */
        $scope.totalData = {};


        /**
         * Query all metadata values for a given metadata name
         * @param id
         */
        var queryMetadataValues = function(id) {
            $http.get(REST_BACKEND_SERVER + '/rest/statistics/meta/spectra/count/' + id)
              .success(function(data) {
                  if (data.length > 0) {
                      $scope.fieldData[data[0].name] = data;
                  }
              });
        };

        /**
         * Query for total statistics
         */
        var queryTotalStatistics = function() {
            return $http.get(REST_BACKEND_SERVER + '/rest/statistics/countAll/')
              .success(function(data) {
                  $scope.totalData = data;
              });
        };


        /**
         * Submit query from clicked metadata link
         * @param name
         * @param value
         */
        $scope.submitQuery = function(name, value) {
            var query = {};
            query[name] = value;

            SpectraQueryBuilderService.compileQuery(query);
            $location.path("/spectra/browse/");
        };


        /**
         * initialization and population of metadata values
         */
        (function list() {
            //MetadataService.metadata(
            //    function(data) {
            //        for(var i = 0; i < data.length; i++) {
            //            for(var j = 0; j < $scope.fields.length; j++) {
            //                if(data[i].name == $scope.fields[j]) {
            //                    queryMetadataValues(data[i].id);
            //                    break;
            //                }
            //            }
            //        }
            //    },
            //    function (error) {
            //        $log.error('metadata failed: ' + error);
            //    }
            //);

            // Temporary fix
            queryMetadataValues(112); // ion mode
            queryMetadataValues(107); // instrument type
            queryMetadataValues(676); // ms type
            queryMetadataValues(117); // precursor type
            queryMetadataValues(2079592); // derivative type

            queryTotalStatistics();
        })();
    }
})();

