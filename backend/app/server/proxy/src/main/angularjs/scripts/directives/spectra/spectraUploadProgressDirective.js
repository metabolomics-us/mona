/**
 * Created by sajjan on 12/19/14.
 */


(function() {
    'use strict';

    spectraUploadProgressBarController.$inject = ['$scope', 'UploadLibraryService'];
    angular.module('moaClientApp')
        .directive('spectraUploadProgressBar', spectraUploadProgressBar);

    function spectraUploadProgressBar() {
        return {
            restrict: 'AE',
            replace: false,
            template:
                '<div data-ng-if="spectraUploadProgress != -1">' +
                '    <div class="text-center"><i>Processed {{completedSpectraCount}} / {{uploadedSpectraCount}} spectra.</i></div>' +
                '    <progressbar ng-class="{active: spectraUploadProgress < 100, \'progress-striped\': spectraUploadProgress < 100, \'progress-bar-success\': spectraUploadProgress == 100}" max="100" value="spectraUploadProgress">' +
                '        <span style="color: black; white-space: nowrap; font-style: italic; font-weight: bold;" data-ng-bind="spectraUploadProgressString"></span>' +
                '    </progressbar>' +
                '    <div class="text-center">{{etaString}}</div>' +
                '</div>' +
                '<div data-ng-if="spectraUploadProgress == -1"><i>No Upload Started</i></div>',
            controller: spectraUploadProgressBarController
        };
    }

    /**
     * watches for changes to the upload progress
     * @param $scope
     */
    /* @ngInject */
    function spectraUploadProgressBarController($scope, UploadLibraryService) {
        $scope.etaString = '';

        var buildEtaString = function() {
            if (UploadLibraryService.uploadStartTime === -1 || !UploadLibraryService.isUploading()) {
                $scope.etaString = '';
            } else if ($scope.completedSpectraCount === 0) {
                $scope.etaString = 'Loading spectra for processing...'

            } else {
                // Calculate estimated time remaining
                var dt = new Date().getTime() - UploadLibraryService.uploadStartTime;
                var eta = dt * ($scope.uploadedSpectraCount - $scope.completedSpectraCount) / $scope.completedSpectraCount / 1000;

                var seconds = Math.floor(eta % 60);
                var minutes = Math.floor((eta / 60) % 60);
                var hours = Math.floor(eta / 3600);

                var etaString = '';

                if (hours > 0) etaString += ' ' + hours + ' hours';
                if (minutes > 0 || hours > 0) etaString += ' ' + minutes + ' minutes';
                if (seconds > 0 || minutes > 0 || hours > 0) etaString += ' ' + seconds + ' seconds';

                if (etaString !== '') {
                    $scope.etaString = 'Estimated' + etaString + ' remaining';
                } else {
                    $scope.etaString = '';
                }
            }
        };

        $scope.$on('spectra:uploadprogress', function(event, completedSpectraCount, failedSpectraCount, uploadedSpectraCount) {
            $scope.completedSpectraCount = completedSpectraCount + failedSpectraCount;
            $scope.failedSpectraCount = failedSpectraCount;
            $scope.uploadedSpectraCount = uploadedSpectraCount;

            $scope.spectraUploadProgress = parseInt((($scope.completedSpectraCount / $scope.uploadedSpectraCount) * 100), 10);
            $scope.spectraUploadProgressString = $scope.spectraUploadProgress + '%';
            buildEtaString();
        });

        (function() {
            if (UploadLibraryService.isUploading()) {
                // Temporarily counting completed and failed uploads together
                $scope.completedSpectraCount = UploadLibraryService.completedSpectraCount + UploadLibraryService.failedSpectraCount;
                $scope.uploadedSpectraCount = UploadLibraryService.uploadedSpectraCount;

                $scope.spectraUploadProgress = parseInt((($scope.completedSpectraCount / $scope.uploadedSpectraCount) * 100), 10);
                $scope.spectraUploadProgressString = $scope.spectraUploadProgress + '%';
            } else {
                $scope.spectraUploadProgress = -1;
                $scope.spectraUploadProgressString = 'Processing...';
            }

            buildEtaString();
        })();
    }
})();