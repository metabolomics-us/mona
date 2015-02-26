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
            '<div ng-if="spectraUploadProgress > -1">'+
            '    <div class="text-center"><i>Processed {{completedSpectraCount}} / {{uploadedSpectraCount}} spectra</i></div>'+
            '    <progressbar ng-class="{active: running, \'progress-striped\': running}" max="100" value="spectraUploadProgress">'+
            '        <span style="color: black; white-space: nowrap; font-style: italic; font-weight: bold;" ng-bind="spectraUploadProgressString"></span>'+
            '    </progressbar>'+
            '</div>'+
            '<div ng-if="spectraUploadProgress == -1"><i>No Upload Started</i></div>',

        /**
         * watches for changes to the upload progress
         * @param $scope
         */
        controller: function ($scope) {
            $scope.spectraUploadProgress = -1;
            $scope.spectraUploadProgressString = 'Processing...';
            $scope.running = false;

            $scope.$on('spectra:uploadprogress', function(event, completedSpectraCount, uploadedSpectraCount) {
                $scope.completedSpectraCount = completedSpectraCount;
                $scope.uploadedSpectraCount = uploadedSpectraCount;

                $scope.spectraUploadProgress = parseInt(((completedSpectraCount / uploadedSpectraCount) * 100), 10);
                $scope.spectraUploadProgressString = $scope.spectraUploadProgress +'%';

                $scope.running = ($scope.spectraUploadProgress != -1 && $scope.spectraUploadProgress < 100);
            });
        }
    };
});