/**
 * Created by wohlgemuth on 6/16/15.
 */

import * as angular from 'angular';

class SpectraDownloadDirective {
    constructor() {
        return {
            require: "ngModel",
            restrict: "A",
            templateUrl: '../../views/templates/spectra/download.html',
            replace: true,
            scope: {
                spectrum: '=spectrum'
            },
            controller: SpectraDownloadController,
            controllerAs: '$ctrl'
        };
    }
}

class SpectraDownloadController {
    private static $inject = ['$scope', 'SpectraQueryBuilderService', 'dialogs', '$http', '$filter', '$log', 'REST_BACKEND_SERVER'];
    private $scope;
    private SpectraQueryBuilderService;
    private dialogs;
    private $http;
    private $filter;
    private $log;
    private REST_BACKEND_SERVER;

    constructor($scope, SpectraQueryBuilderService, dialogs, $http, $filter, $log, REST_BACKEND_SERVER) {
        this.$scope = $scope;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
        this.dialogs = dialogs;
        this.$filter = $filter;
        this.$http = $http;
        this.$log = $log;
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
    }

    /**
     * Emulate the downloading of a file given its contents and name
     * @param data
     * @param filename
     * @param mimetype
     */
    downloadData = (data, filename, mimetype) => {
        let blob = new Blob([data], {type: mimetype});

        if (window.navigator.msSaveOrOpenBlob) {
            // IE 10 Hack
            window.navigator.msSaveBlob(blob, filename);
        } else {
            let hiddenElement = document.createElement('a');
            hiddenElement.href = (window.URL || window.webkitURL).createObjectURL(blob);
            // hiddenElement.target = '_blank';
            hiddenElement.download = filename;

            document.body.appendChild(hiddenElement);
            hiddenElement.click();
            document.body.removeChild(hiddenElement);
        }
    };

    /**
     * attempts to download a msp file
     */
    downloadAsMSP = () => {
        if (angular.isDefined(this.$scope.spectrum)) {
            this.$http({
                method: 'GET',
                url: this.REST_BACKEND_SERVER +'/rest/spectra/'+ this.$scope.spectrum.id,
                headers: {'Accept': 'text/msp'}
            }).then((returnData) => {
                this.downloadData(returnData.data, this.$scope.spectrum.id + '.msp', 'text/msp');
            });
        } else {
            let query = angular.copy(this.SpectraQueryBuilderService.getQuery());
            query.format = 'msp';

            this.submitQueryExportRequest(query);
        }
    };

    /**
     * attempts to download as a mona file
     */
    downloadAsJSON = () => {
        if (angular.isDefined(this.$scope.spectrum)) {
            this.$http({
                method: 'GET',
                url: this.REST_BACKEND_SERVER +'/rest/spectra/'+ this.$scope.spectrum.id,
                headers: {'Accept': 'application/json'}
            }).then((response) => {
                this.downloadData(this.$filter('json')(response.data), this.$scope.spectrum.id + '.json', 'application/json');
            });
        } else {
            let query = angular.copy(this.SpectraQueryBuilderService.getQuery());
            query.format = 'json';

            this.submitQueryExportRequest(query);
        }
    };

    /**
     * submit query for exporting and show modal dialog response
     */
    submitQueryExportRequest = (query) => {
        let uri = this.REST_BACKEND_SERVER + '/rest/spectra/search/export';

        this.$http.post(uri, query).then(
            (response) => {
                this.dialogs.notify('Export request successful!', 'Your query export request has been submitted.  ' +
                    'You will receive an email with a download link when the export has been completed.  ' +
                    'This can take up to 24 hours for very large queries.',
                    {size: 'md', backdrop: 'static'});
            },
            (response) => {
                this.dialogs.error('Error submitting request!',
                    response.status === 403 ? "You must be logged in to request a query export." :
                        "Could not reach MoNA server!",
                    {size: 'md', backdrop: 'static'});
            }
        );
    }
}

angular.module('moaClientApp')
    .directive('spectraDownload', SpectraDownloadDirective);
