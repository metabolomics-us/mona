/**
 * Created by sajjan on 12/19/14.
 */


import * as angular from 'angular';

class SpectraUploadProgressDirective {
    constructor() {
        return {
            restrict: 'AE',
            replace: false,
            template:
                '<div data-ng-if="$ctrl.spectraUploadProgress != -1">' +
                '    <div class="text-center"><i>Processed {{$ctrl.completedSpectraCount}} / {{$ctrl.uploadedSpectraCount}} spectra.</i></div>' +
                '    <uib-progressbar ng-class="{active: $ctrl.spectraUploadProgress < 100, \'progress-striped\': $ctrl.spectraUploadProgress < 100, \'progress-bar-success\': $ctrl.spectraUploadProgress == 100}" max="100" value="$ctrl.spectraUploadProgress">' +
                '        <span style="color: black; white-space: nowrap; font-style: italic; font-weight: bold;" data-ng-bind="$ctrl.spectraUploadProgressString"></span>' +
                '    </uib-progressbar>' +
                '    <div class="text-center">{{$ctrl.etaString}}</div>' +
                '</div>' +
                '<div data-ng-if="$ctrl.spectraUploadProgress == -1"><i>No Upload Started</i></div>',
            controller: SpectraUploadProgressController,
            controllerAs: '$ctrl'
        };
    }
}

class SpectraUploadProgressController {
    private static $inject = ['$scope', 'UploadLibraryService', 'AuthenticationService'];
    private $scope;
    private UploadLibraryService;
    private AuthenticationService;
    private etaString;
    private completedSpectraCount;
    private uploadedSpectraCount;
    private failedSpectraCount;
    private spectraUploadProgress;
    private spectraUploadProgressString;

    constructor($scope, UploadLibraryService, AuthenticationService) {
        this.$scope = $scope;
        this.UploadLibraryService = UploadLibraryService;
        this.AuthenticationService = AuthenticationService;
    }

    $onInit = () => {
        this.etaString = '';

        this.$scope.$on('spectra:uploadprogress', (event, completedSpectraCount, failedSpectraCount, uploadedSpectraCount) => {
            this.completedSpectraCount = completedSpectraCount + failedSpectraCount;
            this.failedSpectraCount = failedSpectraCount;
            this.uploadedSpectraCount = uploadedSpectraCount;

            this.spectraUploadProgress = (this.completedSpectraCount / this.uploadedSpectraCount) * 100;
            this.spectraUploadProgressString = this.spectraUploadProgress + '%';
            this.buildEtaString();
        });

        if (this.UploadLibraryService.isUploading()) {
            // Temporarily counting completed and failed uploads together
            this.completedSpectraCount = this.UploadLibraryService.completedSpectraCount + this.UploadLibraryService.failedSpectraCount;
            this.uploadedSpectraCount = this.UploadLibraryService.uploadedSpectraCount;

            this.spectraUploadProgress = (this.completedSpectraCount / this.uploadedSpectraCount) * 100;
            this.spectraUploadProgressString = this.spectraUploadProgress + '%';
        } else {
            this.spectraUploadProgress = -1;
            this.spectraUploadProgressString = 'Processing...';
        }

        this.buildEtaString();
    }

    buildEtaString = () => {
        if (this.UploadLibraryService.uploadStartTime === -1 || !this.UploadLibraryService.isUploading()) {
            this.etaString = '';
        } else if (this.completedSpectraCount === 0) {
            this.etaString = 'Loading spectra for processing...'

        } else {
            // Calculate estimated time remaining
            let dt = new Date().getTime() - this.UploadLibraryService.uploadStartTime;
            let eta = dt * (this.uploadedSpectraCount - this.completedSpectraCount) / this.completedSpectraCount / 1000;

            let seconds = Math.floor(eta % 60);
            let minutes = Math.floor((eta / 60) % 60);
            let hours = Math.floor(eta / 3600);

            let etaString = '';

            if (hours > 0) etaString += ' ' + hours + ' hours';
            if (minutes > 0 || hours > 0) etaString += ' ' + minutes + ' minutes';
            if (seconds > 0 || minutes > 0 || hours > 0) etaString += ' ' + seconds + ' seconds';

            if (etaString !== '') {
                this.etaString = 'Estimated' + etaString + ' remaining';
            } else {
                this.etaString = '';
            }
        }
    };
}

angular.module('moaClientApp')
    .directive('spectraUploadProgressBar', SpectraUploadProgressDirective);
