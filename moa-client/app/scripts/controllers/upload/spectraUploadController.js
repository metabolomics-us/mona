/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SpectraUploadController = function ($scope, $modal) {
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
    };
};