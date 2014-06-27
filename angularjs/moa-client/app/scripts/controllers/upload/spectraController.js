/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SpectraController = function ($scope, $modal, CTSService, Spectrum, AuthentificationService, $filter, $upload, UploadLibraryService,$log) {

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

            //uggly hack for now
            spectra.biologicalCompound.names = [
                {name: spectra.biologicalCompound.names}
            ];
            spectra.chemicalCompound.names = [
                {name: spectra.chemicalCompound.names}
            ];

            spectra.$save();
        });
    };

    /**
     * builds a spectrum object for us to use
     */
    $scope.buildSpectrum = function () {

        var spectrum = new Spectrum();
        spectrum.biologicalCompound = {names: []};
        spectrum.chemicalCompound = {names: []};
        spectrum.tags = [];
        spectrum.metaData = [];

        return spectrum;
    };

    /**
     * uploads a msp library to the system
     */
    $scope.uploadLibrary = function (files) {
        UploadLibraryService.uploadMSP(files, function () {
            return $scope.buildSpectrum()
        }, function (spectra) {
            $log.debug("storing spectra: \n\n" + $filter('json')(spectra));
            spectra.$save();
        });
    }
};

