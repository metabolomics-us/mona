/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SpectraController = function ($scope, $modal, CTSService, Spectrum, AuthentificationService, $filter, $upload, UploadLibraryService) {

    /**
     * initializes our spectra upload dialog
     */
    $scope.uploadSpectraDialog = function () {
        var modalInstance = $modal.open({
            templateUrl: '/views/upload/dialog/wizard.html',
            controller: moaControllers.SpectraWizardController,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                newSpectrum: function () {

                    return $scope.buildSpectrum();
                }
            }
        });

        //retrieve the result from the dialog and save it
        modalInstance.result.then(function (spectra) {
            spectra.$save();
        });
    };


    /**
     * uploads an existing spectrum to the system should be a modal dialog
     */
    $scope.uploadDummySpectrum = function () {
        var spectrum = angular.fromJson($scope.jsonData);

        var modalInstance = $modal.open({
            templateUrl: '/views/upload/dialog/wizard.html',
            controller: moaControllers.SpectraWizardController,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                newSpectrum: function () {

                    return new Spectrum(spectrum);
                }
            }
        });

        //retrieve the result from the dialog and save it
        modalInstance.result.then(function (spectra) {
            spectra.$save();
        });

    };

    /**
     * builds a spectrum object for us to use
     */
    $scope.buildSpectrum = function () {

        var spectrum = new Spectrum();
        spectrum.biologicalCompound = {};
        spectrum.chemicalCompound = {};
        spectrum.tags = [];
        spectrum.metadata = [];

        return spectrum;
    };

    /**
     * uploads a msp library to the system
     */
    $scope.uploadLibrary = function (files) {
        UploadLibraryService.uploadMSP(files, function () {
            return $scope.buildSpectrum()
        });
    }
};

