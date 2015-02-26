/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SpectraUploadController = function ($scope, $modal, UploadLibraryService) {
    $scope.spectraUploaded = false;
    $scope.spectraUploadProgress = 0.0;


    /**
     * Initializes our spectra upload dialog
     */
    $scope.uploadSpectraDialog = function () {
        var modalInstance = $modal.open({
            templateUrl: '/views/spectra/upload/uploadSpectraWizardModal.html',
            controller: moaControllers.SpectraUploadWizardController,
            size: 'lg',
            backdrop: 'static'
        });

        modalInstance.result.then(function (result) {
            if(result) {
                $scope.spectraUploaded = true;
            }
        });
    };

    $scope.$on('spectra:uploadprogress', function(event, completedSpectraCount, uploadedSpectraCount) {
        $scope.spectraUploadProgress = parseInt(((completedSpectraCount / uploadedSpectraCount) * 100), 10);
    });
};