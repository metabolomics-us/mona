/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SpectraController = function ($scope, $modal) {
    /**
     * Initializes our spectra upload dialog
     */
    $scope.uploadSpectraDialog = function () {
        var modalInstance = $modal.open({
            templateUrl: '/views/spectra/upload/wizards/uploadSpectraWizardModal.html',
            controller: moaControllers.SpectraWizardController,
            size: 'lg',
            backdrop: 'static'
        });
    };
};