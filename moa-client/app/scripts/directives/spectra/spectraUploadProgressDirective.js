/**
 * Created by sajjan on 12/19/14.
 */

'use strict';


app.directive('spectraUploadProgressBar', function () {
    return {
        //must be an attribute
        restrict: 'E',
        replace: false,
        template:
            '<div ng-if="running">'+
            '    <div class="text-center"><i>Processed {{completedSpectraCount}} / {{uploadedSpectraCount}} spectra.</i></div>'+
            '    <progressbar ng-class="{active: running, \'progress-striped\': running}" max="100" value="spectraUploadProgress">'+
            '        <span style="color: black; white-space: nowrap; font-style: italic; font-weight: bold;" ng-bind="spectraUploadProgressString"></span>'+
            '    </progressbar>'+
            '    <div class="text-center">{{etaString}}</div>'+
            '</div>'+
            '<div ng-if="!running"><i>No Upload Started</i></div>',

        /**
         * watches for changes to the upload progress
         * @param $scope
         */
        controller: function ($scope, UploadLibraryService) {
            $scope.etaString = '';

            var buildEtaString = function() {
                if (UploadLibraryService.uploadStartTime == -1 || !UploadLibraryService.isUploading()) {
                    $scope.etaString = '';
                }

                if ($scope.completedSpectraCount == 0) {
                    $scope.etaString = 'Starting upload...'
                }

                // Calculate estimated time remaining
                var dt = new Date().getTime() - UploadLibraryService.uploadStartTime;
                var eta = dt * ($scope.uploadedSpectraCount - $scope.completedSpectraCount) / $scope.completedSpectraCount / 1000;

                var seconds = Math.floor(eta % 60);
                var minutes = Math.floor((eta / 60) % 60);
                var hours = Math.floor(eta / 3600);

                var etaString = '';

                if (hours > 0) etaString += ' '+ hours +' hours';
                if (minutes > 0 || hours > 0) etaString += ' '+ minutes +' minutes';
                if (seconds > 0 || minutes > 0 || hours > 0) etaString += ' '+ seconds +' seconds';

                if (etaString != '') {
                    $scope.etaString = 'Estimated' + etaString + ' remaining';
                } else {
                    $scope.etaString = '';
                }
            };

            $scope.$on('spectra:uploadprogress', function(event, completedSpectraCount, uploadedSpectraCount) {
                $scope.completedSpectraCount = completedSpectraCount;
                $scope.uploadedSpectraCount = uploadedSpectraCount;

                $scope.spectraUploadProgress = parseInt(((completedSpectraCount / uploadedSpectraCount) * 100), 10);
                $scope.spectraUploadProgressString = $scope.spectraUploadProgress +'%';
                buildEtaString();

                $scope.running = $scope.spectraUploadProgress != -1;
            });

            (function() {
                if (UploadLibraryService.isUploading()) {
                    $scope.spectraUploadProgress = parseInt(((UploadLibraryService.completedSpectraCount / UploadLibraryService.uploadedSpectraCount) * 100), 10);
                    $scope.spectraUploadProgressString = $scope.spectraUploadProgress +'%';
                    $scope.running = $scope.spectraUploadProgress != -1;
                } else {
                    $scope.spectraUploadProgress = -1;
                    $scope.spectraUploadProgressString = 'Processing...';
                    $scope.running = false;
                }

                buildEtaString();
            })();
        }
    };
});