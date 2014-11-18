/**
 * Created by Gert on 5/28/2014.
 */
'use strict';

moaControllers.SpectraController = function ($scope, $modal, Spectrum, AuthentificationService, $filter, $upload, UploadLibraryService, $log, $location) {
    /**
     * initializes our spectra upload dialog
     */
    $scope.uploadSpectraDialog = function () {
        var modalInstance = $modal.open({
            templateUrl: '/views/spectra/upload/wizards/uploadNewSpectraWizard.html',
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
            //ugly hack for now
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

        for (var i in files) {

            var fileReader = new FileReader();

            /**
             * once the data is actually loaded
             * @param e
             */
            fileReader.onload = (function (file) {

                /**
                 * handles an event
                 */
                return function (e) {

                    var data = e.target.result;

                    UploadLibraryService.upload(data, function () {
                        return $scope.buildSpectrum()
                    }, function (spectra) {
                        $log.debug("storing spectra: \n\n" + $filter('json')(spectra));
                        spectra.$batchSave();
                    },file.name);

                    fireUploadProgress(100);
                }
            })(files[i]);

            /**
             * progress notification
             * @param event
             */
            fileReader.onprogress = function (event) {
                if (event.lengthComputable) {
                    var progress = parseInt(((event.loaded / event.total) * 100), 10);
                    fireUploadProgress(progress);
                }
            };

            //start the reading
            fileReader.readAsText(files[i]);

        }

        /**
         * handles feedback for progress bars only during the upload!
         * @param progress
         */
        var fireUploadProgress = function (progress) {
        };
    };
};

