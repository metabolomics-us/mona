/**
 * Created by wohlgemuth on 6/16/15.
 */

(function() {
    'use strict';

    spectraDownloadController.$inject = ['$scope', 'SpectraQueryBuilderService', 'dialogs', '$http', '$filter', '$log', 'REST_BACKEND_SERVER'];
    angular.module('moaClientApp')
      .directive('spectraDownload', spectraDownload);

    function spectraDownload() {
        return {
            require: "ngModel",
            restrict: "A",
            templateUrl: '/views/templates/spectra/download.html',
            replace: true,
            scope: {
                spectrum: '=spectrum'
            },
            controller: spectraDownloadController
        };
    }

    /* @ngInject */
    function spectraDownloadController($scope, SpectraQueryBuilderService, dialogs,
                                       $http, $filter, $log, REST_BACKEND_SERVER) {
        /**
         * Emulate the downloading of a file given its contents and name
         * @param data
         * @param filename
         * @param mimetype
         */
        $scope.downloadData = function(data, filename, mimetype) {
            var hiddenElement = document.createElement('a');

            hiddenElement.href = 'data:'+ mimetype +',' + encodeURIComponent(data);
            hiddenElement.target = '_blank';
            hiddenElement.download = filename;

            document.body.appendChild(hiddenElement);
            hiddenElement.click();
            document.body.removeChild(hiddenElement);
        };

        /**
         * attempts to download a msp file
         */
        $scope.downloadAsMSP = function() {
            if (angular.isDefined($scope.spectrum)) {
                $http({
                    method: 'GET',
                    url: REST_BACKEND_SERVER +'/rest/spectra/'+ $scope.spectrum.id,
                    headers: {'Accept': 'text/msp'}
                }).then(function(returnData) {
                    $scope.downloadData(returnData.data, $scope.spectrum.id + '.msp', 'text/msp');
                });
            } else {
                var query = angular.copy(SpectraQueryBuilderService.getQuery());
                query.format = 'msp';

                submitQueryExportRequest(query);
            }
        };

        /**
         * attempts to download as a mona file
         */
        $scope.downloadAsJSON = function() {
            if (angular.isDefined($scope.spectrum)) {
                $http({
                    method: 'GET',
                    url: REST_BACKEND_SERVER +'/rest/spectra/'+ $scope.spectrum.id,
                    headers: {'Accept': 'application/json'}
                }).then(function(response) {
                    $scope.downloadData($filter('json')(response.data), $scope.spectrum.id + '.json', 'application/json');
                });
            } else {
                var query = angular.copy(SpectraQueryBuilderService.getQuery());
                query.format = 'json';

                submitQueryExportRequest(query);
            }
        };

        /**
         * submit query for exporting and show modal dialog response
         */
        var submitQueryExportRequest = function(query) {
            var uri = REST_BACKEND_SERVER + '/rest/spectra/search/export';

            $http.post(uri, query).then(
              function(response) {
                  dialogs.notify('Export request successful!', 'Your query export request has been submitted.  ' +
                    'You will receive an email with a download link when the export has been completed.  ' +
                    'This can take up to 24 hours for very large queries.',
                    {size: 'md', backdrop: 'static'});
              },
              function(response) {
                  dialogs.error('Error submitting request!',
                    response.status === 403 ? "You must be logged in to request a query export." :
                      "Could not reach MoNA server!",
                    {size: 'md', backdrop: 'static'});
              }
            );
        }
    }
})();