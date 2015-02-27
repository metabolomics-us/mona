/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SpectraUploadController = function ($scope, $modal, AuthenticationService) {
    $scope.isLoggedIn = function() {
        return AuthenticationService.isLoggedIn();
    };


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

    /**
     * Check whether the user is uploading, and open the upload dialog accordingly
     */
    (function() {
        if ($scope.isLoggedIn() && !$scope.spectraUploaded) {
            $scope.uploadSpectraDialog()
        }
    })();
};