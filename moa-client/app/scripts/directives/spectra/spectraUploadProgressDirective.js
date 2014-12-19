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
            '    <progressbar ng-class="{active: running, progress-striped: running}" max="100" value="spectraUploadProgress">'+
            '        <span style="color: black; white-space: nowrap; font-style: italic; font-weight: bold;" ng-bind="spectraUploadProgressString"></span>'+
            '    </progressbar>'+
            '</div>'+
            '<div ng-if="!running"><label>No Upload Started</label></div>',

        /**
         * watches for changes to the upload progress
         * @param $scope
         */
        controller: function ($scope) {
            $scope.spectraUploadProgress = -1;
            $scope.spectraUploadProgressString = 'Processing...';
            $scope.running = false;

            $scope.$on('spectra:uploadprogress', function(event, uploadProgress) {
                $scope.spectraUploadProgress = uploadProgress;
                $scope.spectraUploadProgressString = uploadProgress +'%';
                $scope.running = ($scope.spectraUploadProgress != -1 && $scope.spectraUploadProgress <= 100)
            });
        },

        link: function (scope, element, attrs) {

        }
    };
});